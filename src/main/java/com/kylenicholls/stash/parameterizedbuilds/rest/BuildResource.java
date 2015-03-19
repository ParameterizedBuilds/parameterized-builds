package com.kylenicholls.stash.parameterizedbuilds.rest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.rest.util.ResourcePatterns;
import com.atlassian.stash.rest.util.RestResource;
import com.atlassian.stash.rest.util.RestUtils;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.sun.jersey.spi.resource.Singleton;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

@Path(ResourcePatterns.REPOSITORY_URI)
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ RestUtils.APPLICATION_JSON_UTF8 })
@Singleton
@AnonymousAllowed
public class BuildResource extends RestResource {

	private SettingsService settingsService;
	private Jenkins jenkins;
	private final StashAuthenticationContext authenticationContext;

	public BuildResource(I18nService i18nService,
			SettingsService settingsService, 
			Jenkins jenkins,
			StashAuthenticationContext authenticationContext) {
		super(i18nService);
		this.settingsService = settingsService;
		this.jenkins = jenkins;
		this.authenticationContext = authenticationContext;
	}

	@POST
	@Path(value = "triggerBuild")
	public Response triggerBuild(@Context final Repository repository, @Context UriInfo uriInfo) {
		if (authenticationContext.isAuthenticated()){
			String[] getResults = new String[2];
			Map<String, String> data = new HashMap<String, String>();
			Settings settings = settingsService.getSettings(repository);
			if (settings == null){
				return Response.status(404).build();
			}
			List<Job> settingsList = settingsService.getJobs(settings.asMap());
			Job jobToBuild = resolveJobConfigFromUriMap(uriInfo.getQueryParameters(), settingsList);
			
			if (jobToBuild == null){
				getResults[0] = "error";
				getResults[1] = "Settings not found for this job";
			} else {
				String apiToken = jenkins.getUserToken(authenticationContext.getCurrentUser().getSlug());
				String jobToken = apiToken == null ? jobToBuild.getToken() : null;
				String updatedParams = resolveQueryParamsFromMap(uriInfo.getQueryParameters(), jobToken);
				getResults = jenkins.triggerJob(jobToBuild.getJobName(), updatedParams, apiToken);
			}
			
			data.put("status", getResults[0]);
			data.put("message", getResults[1]);
			return Response.ok(data).build();
		}
		return null;
	}
	
	@GET
	@Path(value = "getJobs")
	public Response getJobs(@Context final Repository repository) {
		if (authenticationContext.isAuthenticated()){
			Settings settings = settingsService.getSettings(repository);
			int count = 0;
			Map<Integer, Object> data = new LinkedHashMap<Integer, Object>();
			if (settings == null){
				return Response.status(404).build();
			}
			for (Job job : settingsService.getJobs(settings.asMap())){
				if (job.getTriggers().contains(Trigger.MANUAL)){
					Map<String, Object> temp = new LinkedHashMap<String, Object>();
					temp.put("id", job.getJobId());
					temp.put("jobName", job.getJobName());
					temp.put("parameters", job.getBuildParameters());
					data.put(count, temp);
					count ++;
				}
			}
			return Response.ok(data).build();
		}
		return null;
	}
	
	protected Job resolveJobConfigFromUriMap(MultivaluedMap<String, String> queryParameters, List<Job> settingsList) {
		for (Job job : settingsList){
			if (Integer.parseInt(queryParameters.get("id").get(0)) == job.getJobId()){
				return job;
			}
		}
		return null;
	}
	
	protected String resolveQueryParamsFromMap(MultivaluedMap<String, String> queryParameters, String token) {
		String queryParams = "";
		Iterator<Entry<String, List<String>>> it = queryParameters.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, List<String>> pair = (Map.Entry<String, List<String>>)it.next();
			queryParams += pair.getKey() + "=" + pair.getValue().get(0) + (it.hasNext() ? "&" : "");
	        it.remove();
	    }
	    if (token != null){
	    	queryParams += "&token=" + token;
	    }
		return queryParams;
	}
}