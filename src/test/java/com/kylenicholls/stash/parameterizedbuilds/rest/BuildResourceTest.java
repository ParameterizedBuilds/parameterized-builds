package com.kylenicholls.stash.parameterizedbuilds.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
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
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class BuildResourceTest {
	private final String REPO_SLUG = "reposlug";
	private final String PROJECT_KEY = "projkey";
	private BuildResource rest;
	private Jenkins jenkins;
	private Repository repository;
	private AuthenticationContext authContext;
	private SettingsService settingsService;
	private Settings settings;
	private UriInfo uriInfo;
	private ApplicationUser user;
	private List<Job> jobs;
	private final Server globalServer = new Server("globalurl", "globaluser", "globaltoken", false);

	@Before
	public void setup() throws Exception {
		I18nService i18nService = mock(I18nService.class);
		settingsService = mock(SettingsService.class);
		jenkins = mock(Jenkins.class);
		authContext = mock(AuthenticationContext.class);
		rest = new BuildResource(i18nService, settingsService, jenkins, authContext);

		repository = mock(Repository.class);
		settings = mock(Settings.class);
		uriInfo = mock(UriInfo.class);
		Project project = mock(Project.class);

		when(authContext.isAuthenticated()).thenReturn(true);
		when(authContext.getCurrentUser()).thenReturn(user);
		when(settingsService.getSettings(repository)).thenReturn(settings);
		when(repository.getProject()).thenReturn(project);
		when(jenkins.getJenkinsServer()).thenReturn(globalServer);
		when(repository.getSlug()).thenReturn(REPO_SLUG);
		when(project.getKey()).thenReturn(PROJECT_KEY);

		jobs = new ArrayList<>();
		when(settingsService.getJobs(any())).thenReturn(jobs);
	}

	@Test
	public void testTriggerBuildNotAuthed() {
		when(authContext.isAuthenticated()).thenReturn(false);
		Response actual = rest.triggerBuild(repository, null, null);

		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), actual.getStatus());
	}

	@Test
	public void testTriggerBuildNoRepoSettings() {
		when(settingsService.getSettings(repository)).thenReturn(null);
		Response actual = rest.triggerBuild(repository, null, null);

		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), actual.getStatus());
	}

	@Test
	public void testTriggerBuildNoMatchingJob() {
		Job job = new Job.JobBuilder(1).triggers(new String[] { "add" }).build();
		jobs.add(job);
		Response actual = rest.triggerBuild(repository, "0", null);

		Map<String, Object> expected = new LinkedHashMap<>();
		expected.put("message", "No settings found for this job");
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), actual.getStatus());
		assertEquals(expected, actual.getEntity());
	}

	@Test
	public void testTriggerBuildWithQueryParams() {
		JenkinsResponse message = new JenkinsResponse.JenkinsMessage().error(false).build();
		Job job = new Job.JobBuilder(0).jobName("job").build();
		jobs.add(job);
		MultivaluedMap<String, String> query = new MultivaluedMapImpl();
		query.add("param1", "value1");
		query.add("param2", "value2");
		when(uriInfo.getQueryParameters()).thenReturn(query);
		when(jenkins.triggerJob(any(), any(), anyBoolean())).thenReturn(message);
		Response results = rest.triggerBuild(repository, "0", uriInfo);

		assertEquals(Response.Status.OK.getStatusCode(), results.getStatus());
		assertEquals(message.getMessage(), results.getEntity());
	}

	@Test
	public void testGetJobsNotAuthed() {
		when(authContext.isAuthenticated()).thenReturn(false);
		Response actual = rest.getJobs(repository, "branch", "commit", null);

		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), actual.getStatus());
	}

	@Test
	public void testGetJobsNoRepoSettings() {
		when(settingsService.getSettings(repository)).thenReturn(null);
		Response actual = rest.getJobs(repository, "branch", "commit", null);

		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), actual.getStatus());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetJobsNoManualJob() {
		Job job = new Job.JobBuilder(1).triggers(new String[] { "add" }).build();
		jobs.add(job);
		Response actual = rest.getJobs(repository, "branch", "commit", null);

		assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
		assertEquals(new ArrayList<Map<String, Object>>(), (List<Map<String, Object>>) actual
				.getEntity());
	}

	@Test
	public void testGetJobsWithPR() {
		int jobId = 1;
		String jobName = "jobname";
		Job job = new Job.JobBuilder(1).jobName(jobName).triggers(new String[] { "manual" })
				.buildParameters("param1=$BRANCH\r\nparam2=$PRDESTINATION").build();
		jobs.add(job);
		Response actual = rest.getJobs(repository, "branch", "commit", "prbranch");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> jobData = (List<Map<String, Object>>) actual.getEntity();
		List<Map<String, Object>> parameters = new ArrayList<>();
		Map<String, Object> parameter1 = new HashMap<>();
		parameter1.put("param1", "branch");
		Map<String, Object> parameter2 = new HashMap<>();
		parameter2.put("param2", "prbranch");
		parameters.add(parameter1);
		parameters.add(parameter2);
		assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
		assertEquals(jobId, jobData.get(0).get("id"));
		assertEquals(jobName, jobData.get(0).get("jobName"));
		assertEquals(parameters, jobData.get(0).get("buildParameters"));
	}

	@Test
	public void testGetJobsWithNoPR() {
		String jobName = "jobname";
		Job job = new Job.JobBuilder(1).jobName(jobName).triggers(new String[] { "manual" })
				.buildParameters("param2=$PRDESTINATION").build();
		jobs.add(job);
		Response actual = rest.getJobs(repository, "branch", "commit", null);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> jobData = (List<Map<String, Object>>) actual.getEntity();
		List<Map<String, Object>> parameters = new ArrayList<>();
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("param2", "$PRDESTINATION");
		parameters.add(parameter);
		assertEquals(parameters, jobData.get(0).get("buildParameters"));
	}
}
