package com.kylenicholls.stash.parameterizedbuilds.rest;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.rest.RestResource;
import com.atlassian.bitbucket.rest.util.ResourcePatterns;
import com.atlassian.bitbucket.rest.util.RestUtils;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;
import com.sun.jersey.spi.resource.Singleton;

@Path(ResourcePatterns.REPOSITORY_URI)
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ RestUtils.APPLICATION_JSON_UTF8 })
@Singleton
@AnonymousAllowed
public class BuildResource extends RestResource {

	private SettingsService settingsService;
	private Jenkins jenkins;
	private final AuthenticationContext authenticationContext;

	public BuildResource(I18nService i18nService, SettingsService settingsService, Jenkins jenkins,
			AuthenticationContext authenticationContext) {
		super(i18nService);
		this.settingsService = settingsService;
		this.jenkins = jenkins;
		this.authenticationContext = authenticationContext;
	}

	@POST
	@Path(value = "triggerBuild/{id}")
	public Response triggerBuild(@Context final Repository repository, @PathParam("id") String id,
			@Context UriInfo uriInfo) {
		if (authenticationContext.isAuthenticated()) {
			Map<String, Object> data = new LinkedHashMap<String, Object>();
			Settings settings = settingsService.getSettings(repository);
			if (settings == null) {
				data.put("message", "No hook settings were found for this repository");
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			List<Job> settingsList = settingsService.getJobs(settings.asMap());
			Job jobToBuild = resolveJobConfigFromUriMap(Integer.parseInt(id), settingsList);

			if (jobToBuild == null) {
				data.put("message", "No settings found for this job");
				return Response.status(Response.Status.NOT_FOUND).entity(data).build();
			} else {
				String userToken = jenkins.getUserToken(authenticationContext.getCurrentUser());
				String updatedParams = resolveQueryParamsFromMap(uriInfo.getQueryParameters());
				Map<String, Object> message = jenkins
						.triggerJob(jobToBuild, updatedParams, userToken).getMessage();
				return Response.ok(message).build();
			}
		}
		return Response.status(Response.Status.FORBIDDEN).build();
	}

	@GET
	@Path(value = "getJobs")
	public Response getJobs(@Context final Repository repository) {
		if (authenticationContext.isAuthenticated()) {
			Settings settings = settingsService.getSettings(repository);
			int count = 0;
			Map<Integer, Object> data = new LinkedHashMap<Integer, Object>();
			if (settings == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			for (Job job : settingsService.getJobs(settings.asMap())) {
				if (job.getTriggers().contains(Trigger.MANUAL)) {
					Map<String, Object> temp = new LinkedHashMap<String, Object>();
					temp.put("id", job.getJobId());
					temp.put("jobName", job.getJobName());
					temp.put("parameters", job.getBuildParameters());
					data.put(count, temp);
					count++;
				}
			}
			return Response.ok(data).build();
		}
		return Response.status(Response.Status.FORBIDDEN).build();
	}

	private Job resolveJobConfigFromUriMap(int id, List<Job> settingsList) {
		for (Job job : settingsList) {
			if (job.getJobId() == id) {
				return job;
			}
		}
		return null;
	}

	private String resolveQueryParamsFromMap(MultivaluedMap<String, String> queryParameters) {
		String queryParams = "";
		Iterator<Entry<String, List<String>>> it = queryParameters.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<String>> pair = (Map.Entry<String, List<String>>) it.next();
			queryParams += pair.getKey() + "=" + pair.getValue().get(0) + (it.hasNext() ? "&" : "");
			it.remove();
		}
		return queryParams;
	}
}