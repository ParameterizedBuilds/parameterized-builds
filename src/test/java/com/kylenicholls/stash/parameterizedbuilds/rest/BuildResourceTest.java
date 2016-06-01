package com.kylenicholls.stash.parameterizedbuilds.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.JenkinsResponse;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class BuildResourceTest {
	private BuildResource rest;
	private Jenkins jenkins;
	private Repository repository;
	private AuthenticationContext authContext;
	private SettingsService settingsService;
	private Settings settings;
	private UriInfo uriInfo;
	private ApplicationUser user;

	@Before
	public void setup() throws Exception {
		I18nService i18nService = mock(I18nService.class);
		settingsService = mock(SettingsService.class);
		jenkins = mock(Jenkins.class);
		authContext = mock(AuthenticationContext.class);
		rest = new BuildResource(i18nService, settingsService, jenkins, authContext);

		when(authContext.isAuthenticated()).thenReturn(true);
		when(authContext.getCurrentUser()).thenReturn(user);
		repository = mock(Repository.class);
		settings = mock(Settings.class);
		when(settingsService.getSettings(repository)).thenReturn(settings);
		uriInfo = mock(UriInfo.class);
		Project project = mock(Project.class);
		when(repository.getProject()).thenReturn(project);
	}

	@Test
	public void testTriggerJob403IfNotAuthed() {
		when(authContext.isAuthenticated()).thenReturn(false);
		Response actual = rest.triggerBuild(repository, null, null);

		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), actual.getStatus());
	}

	@Test
	public void testTriggerJob404IfNoRepoSettings() {
		when(settingsService.getSettings(repository)).thenReturn(null);
		Response actual = rest.triggerBuild(repository, null, null);

		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), actual.getStatus());
	}

	@Test
	public void testTriggerJobNoMatchingJob() {
		Job job = new Job.JobBuilder(1).triggers(new String[] { "add" }).createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		Response actual = rest.triggerBuild(repository, "0", null);

		Map<String, Object> expected = new LinkedHashMap<String, Object>();
		expected.put("message", "No settings found for this job");
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), actual.getStatus());
		assertEquals(expected, actual.getEntity());
	}

	@Test
	public void testTriggerJobWithQueryParams() {
		JenkinsResponse message = new JenkinsResponse.JenkinsMessage().error(false).build();
		Job job = new Job.JobBuilder(0).createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		MultivaluedMap<String, String> query = new MultivaluedMapImpl();
		query.add("param1", "value1");
		query.add("param2", "value2");
		when(uriInfo.getQueryParameters()).thenReturn(query);
		when(jenkins.triggerJob(job, "param1=value1&param2=value2", user, repository.getProject()
				.getKey())).thenReturn(message);
		Response results = rest.triggerBuild(repository, "0", uriInfo);

		assertEquals(Response.Status.OK.getStatusCode(), results.getStatus());
		assertEquals(message.getMessage(), results.getEntity());
	}

	@Test
	public void testGetJobs403IfNotAuthed() {
		when(authContext.isAuthenticated()).thenReturn(false);
		Response actual = rest.getJobs(repository);

		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), actual.getStatus());
	}

	@Test
	public void testGetJobs404IfNoRepoSettings() {
		when(settingsService.getSettings(repository)).thenReturn(null);
		Response actual = rest.getJobs(repository);

		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), actual.getStatus());
	}

	@Test
	public void testGetJobsNoManualJob() {
		Job job = new Job.JobBuilder(1).triggers(new String[] { "add" }).createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		Response actual = rest.getJobs(repository);

		assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
		assertEquals(new HashMap<>(), actual.getEntity());
	}

	@Test
	public void testGetJobs() {
		int jobId = 1;
		String jobName = "jobname";
		Job job = new Job.JobBuilder(1).jobName(jobName).triggers(new String[] { "manual" })
				.buildParameters("param1=value1").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		Response actual = rest.getJobs(repository);

		Map<String, Object> jobData = new LinkedHashMap<>();
		jobData.put("id", jobId);
		jobData.put("jobName", jobName);
		Map<String, String> jobParams = new LinkedHashMap<>();
		jobParams.put("param1", "value1");
		jobData.put("parameters", jobParams);
		Map<Integer, Object> expected = new LinkedHashMap<>();
		expected.put(0, jobData);
		assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
		assertEquals(expected, actual.getEntity());
	}
}
