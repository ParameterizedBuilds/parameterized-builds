package com.kylenicholls.stash.parameterizedbuilds.rest;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.rest.RestResource;
import com.atlassian.bitbucket.rest.util.ResourcePatterns;
import com.atlassian.bitbucket.rest.util.RestUtils;
import com.atlassian.bitbucket.server.ApplicationPropertiesService;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.conditions.BuildPermissionsCondition;
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
	private final ApplicationPropertiesService applicationPropertiesService;
	private final PullRequestService prService;
	private final AuthenticationContext authContext;
	private final BuildPermissionsCondition permissionsCheck;

	public BuildResource(I18nService i18nService, SettingsService settingsService, Jenkins jenkins,
			ApplicationPropertiesService applicationPropertiesService,
			PullRequestService prService,
			AuthenticationContext authContext, BuildPermissionsCondition permissionsCheck) {
		super(i18nService);
		this.settingsService = settingsService;
		this.jenkins = jenkins;
		this.applicationPropertiesService = applicationPropertiesService;
		this.prService = prService;
		this.authContext = authContext;
		this.permissionsCheck = permissionsCheck;
	}

	@POST
	@Path(value = "triggerBuild/{id}/{branch}")
	public Response triggerBuild(@Context final Repository repository, @PathParam("id") String id,
								 @PathParam("branch") String branch, @Context UriInfo uriInfo) {
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

				//create a job with already resolved buildParameters
				Map<String, Object> paramList =
						uriInfo.getQueryParameters().entrySet()
								.stream()
								.collect(Collectors.toMap(Entry::getKey, e->e.getValue().get(0)));
				Job job = jobToBuild.copy().buildParameters(paramList).build();

				//create bitbucketVariables with only branch and trigger to resolve pipelines
				BitbucketVariables variables = new BitbucketVariables.Builder()
						.add("$BRANCH", () -> branch)
						.add("$TRIGGER", Trigger.MANUAL::toString).build();

				Map<String, Object> message = jenkins.triggerJob(projectKey, user, job, variables)
						.getMessage();
				return Response.ok(message).build();
			}
		}
		return Response.status(Response.Status.FORBIDDEN).build();
	}

	@GET
	@Path(value = "getJenkinsServers")
	public Response getJenkinsServers(@Context final Repository repository){
		if (authContext.isAuthenticated()) {
			String projectKey = repository.getProject().getKey();
			List<Map<String, String>> servers = new ArrayList<>();

			Optional.ofNullable(jenkins.getJenkinsServer(projectKey))
					.map(x -> createServerMap(x, projectKey)).ifPresent(servers::add);
			Optional.ofNullable(jenkins.getJenkinsServer(null))
					.map(x -> createServerMap(x, null)).ifPresent(servers::add);

			return Response.ok(servers).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).build();
		}
	}

	private Map createServerMap(Server server, String projectKey){
		Map<String, String> serverMap = new HashMap<>();
		serverMap.put("url", server.getBaseUrl());
		serverMap.put("alias", server.getAlias());
		serverMap.put("scope", projectKey == null ? "global": "project");
		serverMap.put("project", projectKey);
		serverMap.put("default_user", server.getUser());
		return serverMap;
	}

	@GET
	@Path(value = "getJobs")
	public Response getJobs(@Context final Repository repository,
			@QueryParam("branch") String branch, @QueryParam("commit") String commit,
			@QueryParam("prdestination") String prDestination, @QueryParam("prid") long prId) {
		if (authContext.isAuthenticated()) {
			Settings settings = settingsService.getSettings(repository);
			if (settings == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}

			String projectKey = repository.getProject().getKey();
			String url = applicationPropertiesService.getBaseUrl().toString();
			Builder variableBuilder = new BitbucketVariables.Builder()
					.populateFromStrings(branch, commit, repository, projectKey, Trigger.MANUAL, url);
			if (prDestination != null) {
				PullRequest pullRequest = prService.getById(repository.getId(), prId);
				if (pullRequest != null) {
					variableBuilder.populateFromPR(pullRequest, repository, projectKey, Trigger.MANUAL, url);
				}
			}

			List<Map<String, Object>> data = new ArrayList<>();
			for (Job job : settingsService.getJobs(settings.asMap())) {
				if (job.getTriggers().contains(Trigger.MANUAL) &&
						permissionsCheck.checkPermissions(job, repository, authContext.getCurrentUser())) {
					data.add(job.asMap(variableBuilder.build()));
				}
			}
			return Response.ok(data).build();
		}
		return Response.status(Response.Status.FORBIDDEN).build();
	}


	@GET
	@Path(value = "getHookEnabled")
	public Response getHookEnabled(@Context final Repository repository) {
		if (authContext.isAuthenticated()) {
			RepositoryHook hook = settingsService.getHook(repository);
			if (hook == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}

			boolean data = hook.isEnabled();
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
