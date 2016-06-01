package com.kylenicholls.stash.parameterizedbuilds.item;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

public class JobTest {
	@Test
	public void testCreateNewBranchJob() {
		int jobId = 0;
		String jobName = "test_job";
		boolean isTag = false;
		String branch = "branch";
		String path = "path";
		String token = "token";
		Job actual = new Job.JobBuilder(jobId).jobName(jobName).isTag(isTag)
				.triggers(new String[] { "add", "manual" })
				.buildParameters("param1=value1\r\nparam2=value2").branchRegex(branch)
				.pathRegex(path).token(token).createJob();

		List<Trigger> triggers = new ArrayList<Trigger>();
		triggers.add(Trigger.ADD);
		triggers.add(Trigger.MANUAL);
		Map<String, String> parameters = new LinkedHashMap<String, String>();
		parameters.put("param1", "value1");
		parameters.put("param2", "value2");

		assertEquals(jobId, actual.getJobId());
		assertEquals(jobName, actual.getJobName());
		assertEquals(isTag, actual.getIsTag());
		assertEquals(triggers, actual.getTriggers());
		assertEquals(parameters, actual.getBuildParameters());
		assertEquals(branch, actual.getBranchRegex());
		assertEquals(path, actual.getPathRegex());
		assertEquals(token, actual.getToken());
	}

	@Test
	public void testCreateNewTagJob() {
		int jobId = 0;
		boolean isTag = true;
		Job actual = new Job.JobBuilder(jobId).isTag(isTag).createJob();

		assertEquals(jobId, actual.getJobId());
		assertEquals(isTag, actual.getIsTag());
	}

	@Test
	public void testAddTriggerNullIfTriggerInvalid() {
		Job actual = new Job.JobBuilder(1).triggers("".split(";")).createJob();

		assertEquals(1, actual.getTriggers().size());
		assertEquals(Trigger.NULL, actual.getTriggers().get(0));
	}

	@Test
	public void testBuildEmptyParameterMap() {
		Job actual = new Job.JobBuilder(1).buildParameters("").createJob();

		assertEquals(new LinkedHashMap<String, String>(), actual.getBuildParameters());
	}

	@Test
	public void testBuildKeyValuePairWithNullValue() {
		Job actual = new Job.JobBuilder(1).buildParameters("param1=").createJob();

		Map<String, String> parameters = new LinkedHashMap<String, String>();
		parameters.put("param1", "");
		assertEquals(parameters, actual.getBuildParameters());
	}

	@Test
	public void testBRANCHVariable() {
		String value = "branchname";
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder().branch(value)
				.build();
		Job actual = new Job.JobBuilder(1).buildParameters("param=$BRANCH").createJob();

		assertEquals("param=" + value, actual.getQueryString(parameters));
	}

	@Test
	public void testCOMMITVariable() {
		String value = "commit";
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder().commit(value)
				.build();
		Job actual = new Job.JobBuilder(1).buildParameters("param=$COMMIT").createJob();

		assertEquals("param=" + value, actual.getQueryString(parameters));
	}

	@Test
	public void testPRDESTINATIONVariable() {
		String value = "branchname";
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder()
				.prDestination(value).build();
		Job actual = new Job.JobBuilder(1).buildParameters("param=$PRDESTINATION").createJob();

		assertEquals("param=" + value, actual.getQueryString(parameters));
	}

	@Test
	public void testREPOSITORYVariable() {
		String value = "branchname";
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder().repoName(value)
				.build();
		Job actual = new Job.JobBuilder(1).buildParameters("param=$REPOSITORY").createJob();

		assertEquals("param=" + value, actual.getQueryString(parameters));
	}

	@Test
	public void testPROJECTVariable() {
		String value = "branchname";
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder()
				.projectName(value).build();
		Job actual = new Job.JobBuilder(1).buildParameters("param=$PROJECT").createJob();

		assertEquals("param=" + value, actual.getQueryString(parameters));
	}

	@Test
	public void testShouldUseFirstOptionForChoiceParams() {
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder().build();
		Job actual = new Job.JobBuilder(1).buildParameters("param=1;2;3").createJob();

		assertEquals("param=1", actual.getQueryString(parameters));
	}

	@Test
	public void testShouldSupportMutlipleParams() {
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder().build();
		Job actual = new Job.JobBuilder(1).buildParameters("param1=value1\r\nparam2=value2")
				.createJob();

		assertEquals("param1=value1&param2=value2", actual.getQueryString(parameters));
	}

	@Test
	public void testBuildUrlNotParameterized() {
		String jobName = "jobname";
		Server server = new Server("url", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).createJob();
		String actual = job.buildUrl(server, "", null);

		assertEquals(server.getBaseUrl() + "/job/jobname/build", actual);
	}

	@Test
	public void testBuildUrlLegacyToken() {
		String jobName = "jobname";
		String token = "token";
		Server server = new Server("url", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).token(token).createJob();
		String actual = job.buildUrl(server, "", null);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/build?token=" + token, actual);
	}

	@Test
	public void testBuildUrlParameterized() {
		String jobName = "jobname";
		String params = "param1=value1";
		Server server = new Server("url", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).createJob();
		String actual = job.buildUrl(server, params, null);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/buildWithParameters?"
				+ params, actual);
	}

	@Test
	public void testBuildUrlParameterizedWithLegacyToken() {
		String jobName = "jobname";
		String token = "token";
		String params = "param1=value1";
		Server server = new Server("url", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).token(token).createJob();
		String actual = job.buildUrl(server, params, null);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/buildWithParameters?" + params
				+ "&token=" + token, actual);
	}

	@Test
	public void testBuildUrlWithLegacyTokenEmpty() {
		String jobName = "jobname";
		Server server = new Server("url", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).token("").createJob();
		String actual = job.buildUrl(server, "", null);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/build", actual);
	}

	@Test
	public void testBuildUrlWithUserTokenAndLegacyToken() {
		String jobName = "jobname";
		Server server = new Server("url", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).token("token").createJob();
		String actual = job.buildUrl(server, "", "usertoken");

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/build", actual);
	}

	@Test
	public void testBuildUrlWithAltUrlAndLegacyToken() {
		String jobName = "jobname";
		String token = "token";
		Server server = new Server("url", "", "", true);
		Job job = new Job.JobBuilder(0).jobName(jobName).token(token).createJob();
		String actual = job.buildUrl(server, "", null);

		assertEquals(server.getBaseUrl() + "/buildByToken/build?job=" + jobName + "&token="
				+ token, actual);
	}

	@Test
	public void testBuildUrlWithAltUrlAndParameters() {
		String jobName = "jobname";
		String params = "param1=value1";
		Server server = new Server("url", "", "", true);
		Job job = new Job.JobBuilder(0).jobName(jobName).createJob();
		String actual = job.buildUrl(server, params, null);

		assertEquals(server.getBaseUrl() + "/buildByToken/buildWithParameters?job=" + jobName + "&"
				+ params, actual);
	}

	@Test
	public void testBuildUrlWithAltUrlAndParametersAndLegacyToken() {
		String jobName = "jobname";
		String token = "token";
		String params = "param1=value1";
		Server server = new Server("url", "", "", true);
		Job job = new Job.JobBuilder(0).jobName(jobName).token(token).createJob();
		String actual = job.buildUrl(server, params, null);

		assertEquals(server.getBaseUrl() + "/buildByToken/buildWithParameters?job=" + jobName + "&"
				+ params + "&token=" + token, actual);
	}

	@Test
	public void testBuildUrlWithAltUrlAndParametersAndUserToken() {
		String jobName = "jobname";
		String params = "param1=value1";
		Server server = new Server("url", "", "", true);
		Job job = new Job.JobBuilder(0).jobName(jobName).createJob();
		String actual = job.buildUrl(server, params, "usertoken");

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/buildWithParameters?"
				+ params, actual);
	}

	@Test
	public void testBuildUrlWithAltUrlAndLegacyTokenAndUserToken() {
		String jobName = "jobname";
		String token = "token";
		Server server = new Server("url", "", "", true);
		Job job = new Job.JobBuilder(0).jobName(jobName).token(token).createJob();
		String actual = job.buildUrl(server, "", "usertoken");

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/build", actual);
	}
}
