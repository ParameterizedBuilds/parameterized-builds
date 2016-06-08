package com.kylenicholls.stash.parameterizedbuilds.rest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.rest.RestResource;
import com.atlassian.bitbucket.rest.util.ResourcePatterns;
import com.atlassian.bitbucket.rest.util.RestUtils;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.BitbucketVariables;
import com.kylenicholls.stash.parameterizedbuilds.item.BitbucketVariables.Builder;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import com.sun.jersey.spi.resource.Singleton;

@Path(ResourcePatterns.REPOSITORY_URI)
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ RestUtils.APPLICATION_JSON_UTF8 })
@Singleton
@AnonymousAllowed
public class BuildResource extends RestResource {
	private SettingsService settingsService;
	private Jenkins jenkins;
	private final AuthenticationContext authContext;

	public BuildResource(I18nService i18nService, SettingsService settingsService, Jenkins jenkins,
			AuthenticationContext authContext) {
		super(i18nService);
		this.settingsService = settingsService;
		this.jenkins = jenkins;
		this.authContext = authContext;
	}

	@POST
	@Path(value = "triggerBuild/{id}")
	public Response triggerBuild(@Context final Repository repository, @PathParam("id") String id,
			@Context UriInfo uriInfo) {
		if (authContext.isAuthenticated()) {
			String projectKey = repository.getProject().getKey();
			Map<String, Object> data = new LinkedHashMap<>();
			Settings settings = settingsService.getSettings(repository);
			if (settings == null) {
				data.put("message", "No build settings were found for this repository");
				return Response.status(Response.Status.NOT_FOUND).entity(data).build();
			}
			List<Job> jobs = settingsService.getJobs(settings.asMap());
			Job jobToBuild = getJobById(Integer.parseInt(id), jobs);

			if (jobToBuild == null) {
				data.put("message", "No settings found for this job");
				return Response.status(Response.Status.NOT_FOUND).entity(data).build();
			} else {
				ApplicationUser user = authContext.getCurrentUser();

				Server jenkinsServer = jenkins.getJenkinsServer(projectKey);
				String joinedUserToken = jenkins.getJoinedUserToken(user, projectKey);
				if (jenkinsServer == null) {
					jenkinsServer = jenkins.getJenkinsServer();
					joinedUserToken = jenkins.getJoinedUserToken(user);
				}

				String buildUrl = jobToBuild.buildManualUrl(jenkinsServer, uriInfo
						.getQueryParameters(), joinedUserToken != null);

				// use default user and token if the user that triggered the
				// build does not have a token set
				boolean prompt = false;
				if (joinedUserToken == null) {
					prompt = true;
					if (!jenkinsServer.getUser().isEmpty()) {
						joinedUserToken = jenkinsServer.getJoinedToken();
					}
				}

				Map<String, Object> message = jenkins.triggerJob(buildUrl, joinedUserToken, prompt)
						.getMessage();
				return Response.ok(message).build();
			}
		}
		return Response.status(Response.Status.FORBIDDEN).build();
	}

	@GET
	@Path(value = "getJobs")
	public Response getJobs(@Context final Repository repository,
			@QueryParam("branch") String branch, @QueryParam("commit") String commit, @QueryParam("prdestination") String prDestination) {
		if (authContext.isAuthenticated()) {
			Settings settings = settingsService.getSettings(repository);
			if (settings == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}

			Builder variableBuilder = new BitbucketVariables.Builder().branch(branch)
					.commit(commit).repoName(repository.getSlug()).projectName(repository.getProject().getKey());
			if (prDestination != null) {
				variableBuilder.prDestination(prDestination);
			}

			List<Map<String, Object>> data = new ArrayList<>();
			for (Job job : settingsService.getJobs(settings.asMap())) {
				if (job.getTriggers().contains(Trigger.MANUAL)) {
					data.add(job.asMap(variableBuilder.build()));
				}
			}
			return Response.ok(data).build();
		}
		return Response.status(Response.Status.FORBIDDEN).build();
	}

	@Nullable
	private Job getJobById(int id, List<Job> jobs) {
		for (Job job : jobs) {
			if (job.getJobId() == id) {
				return job;
			}
		}
		return null;
	}
}