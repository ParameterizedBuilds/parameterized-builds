package com.kylenicholls.stash.parameterizedbuilds.item;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class JobTest {
	@Test
	public void testBuildJobId() {
		int jobId = 0;
		Job actual = new Job.JobBuilder(jobId).build();

		assertEquals(jobId, actual.getJobId());
	}

	@Test
	public void testBuildJobName() {
		String jobName = "test_job";
		Job actual = new Job.JobBuilder(0).jobName(jobName).build();

		assertEquals(jobName, actual.getJobName());
	}

	@Test
	public void testBuildJobBranchRegex() {
		String branchRegex = "branch";
		Job actual = new Job.JobBuilder(0).branchRegex(branchRegex).build();

		assertEquals(branchRegex, actual.getBranchRegex());
	}

	@Test
	public void testBuildJobPathRegex() {
		String pathRegex = "path";
		Job actual = new Job.JobBuilder(0).pathRegex(pathRegex).build();

		assertEquals(pathRegex, actual.getPathRegex());
	}

	@Test
	public void testBuildJobToken() {
		String token = "token";
		Job actual = new Job.JobBuilder(0).token(token).build();

		assertEquals(token, actual.getToken());
	}

	@Test
	public void testBuildJobTriggers() {
		Job actual = new Job.JobBuilder(0).triggers(new String[] { "add", "manual" }).build();

		List<Trigger> triggers = new ArrayList<>();
		triggers.add(Trigger.ADD);
		triggers.add(Trigger.MANUAL);
		assertEquals(triggers, actual.getTriggers());
	}

	@Test
	public void testBuildJobNoBuildParameters() {
		Job actual = new Job.JobBuilder(0).buildParameters("").build();

		assertEquals(0, actual.getBuildParameters().size());
	}

	@Test
	public void testBuildJobTag() {
		boolean isTag = true;
		Job actual = new Job.JobBuilder(0).isTag(isTag).build();

		assertEquals(isTag, actual.getIsTag());
	}

	@Test
	public void testBuildJobInvalidTrigger() {
		Job actual = new Job.JobBuilder(1).triggers("".split(";")).build();

		assertEquals(1, actual.getTriggers().size());
		assertEquals(Trigger.NULL, actual.getTriggers().get(0));
	}

	@Test
	public void testBuildJobBuildParameterKeyNoValue() {
		Job actual = new Job.JobBuilder(1).buildParameters("param1=").build();

		assertEquals(new SimpleEntry<>("param1", ""), actual.getBuildParameters().get(0));
	}

	@Test
	public void testBuildJobBuildParameterChoices() {
		Job actual = new Job.JobBuilder(1).buildParameters("param1=1;2;3").build();

		assertArrayEquals(new String[] { "1", "2", "3" }, (String[]) actual.getBuildParameters()
				.get(0).getValue());
	}

	@Test
	public void testBuildJobBuildParameterBoolean() {
		Job actual = new Job.JobBuilder(1).buildParameters("param1=true").build();

		assertTrue((boolean) actual.getBuildParameters().get(0).getValue());
	}

	@Test
	public void testBuildUrlNullServer() {
		Job job = new Job.JobBuilder(1).build();
		String actual = job.buildUrl(null, null, false);

		assertEquals(null, actual);
	}

	@Test
	public void testBuildUrlNoUserTokenAndUseAltUrl() {
		String jobName = "jobname";
		Server server = new Server("http://baseurl", "", "", true);
		Job job = new Job.JobBuilder(0).jobName(jobName).buildParameters("").build();
		String actual = job.buildUrl(server, new BitbucketVariables.Builder().build(), false);

		assertEquals(server.getBaseUrl() + "/buildByToken/build?job=" + jobName, actual);
	}

	@Test
	public void testBuildUrlUserTokenAndUseAltUrl() {
		String jobName = "jobname";
		Server server = new Server("http://baseurl", "", "", true);
		Job job = new Job.JobBuilder(0).jobName(jobName).buildParameters("").build();
		String actual = job.buildUrl(server, new BitbucketVariables.Builder().build(), true);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/build", actual);
	}

	@Test
	public void testBuildUrlNoUserTokenAndNoUseAltUrl() {
		String jobName = "jobname";
		Server server = new Server("http://baseurl", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).buildParameters("").build();
		String actual = job.buildUrl(server, new BitbucketVariables.Builder().build(), false);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/build", actual);
	}

	@Test
	public void testBuildUrlNoUserTokenAndLegacyToken() {
		String jobName = "jobname";
		String token = "token";
		Server server = new Server("http://baseurl", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).buildParameters("").token(token).build();
		String actual = job.buildUrl(server, new BitbucketVariables.Builder().build(), false);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/build?token=" + token, actual);
	}

	@Test
	public void testBuildUrlUserTokenAndLegacyToken() {
		String jobName = "jobname";
		String token = "token";
		Server server = new Server("http://baseurl", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).buildParameters("").token(token).build();
		String actual = job.buildUrl(server, new BitbucketVariables.Builder().build(), true);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/build", actual);
	}

	@Test
	public void testBuildUrlNoUserTokenAndLegacyTokenEmpty() {
		String jobName = "jobname";
		Server server = new Server("http://baseurl", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).buildParameters("").token("").build();
		String actual = job.buildUrl(server, new BitbucketVariables.Builder().build(), false);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/build", actual);
	}

	@Test
	public void testBuildUrlNotParameterized() {
		String jobName = "jobname";
		Server server = new Server("http://baseurl", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).buildParameters("").build();
		String actual = job.buildUrl(server, new BitbucketVariables.Builder().build(), false);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/build", actual);
	}

	@Test
	public void testBuildUrlParameterized() {
		String jobName = "jobname";
		String params = "param1=value1";
		Server server = new Server("http://baseurl", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).buildParameters(params).build();
		String actual = job.buildUrl(server, new BitbucketVariables.Builder().build(), false);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/buildWithParameters?"
				+ params, actual);
	}

	@Test
	public void testBuildUrlChoiceParameters() {
		String jobName = "jobname";
		String params = "param1=1;2;3";
		Server server = new Server("http://baseurl", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).buildParameters(params).build();
		String actual = job.buildUrl(server, new BitbucketVariables.Builder().build(), false);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/buildWithParameters?"
				+ params.split(";")[0], actual);
	}

	@Test
	public void testBuildUrlWithBitbucketVariables() {
		String jobName = "jobname";
		String params = "param1=$BRANCH";
		String branch = "branchname";
		BitbucketVariables vars = new BitbucketVariables.Builder().branch(branch).build();
		Server server = new Server("http://baseurl", "", "", false);
		Job job = new Job.JobBuilder(0).jobName(jobName).buildParameters(params).build();
		String actual = job.buildUrl(server, vars, false);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/buildWithParameters?"
				+ params.replace("$BRANCH", branch), actual);
	}

	@Test
	public void testBuildJobAsMap() {
		int id = 0;
		String jobName = "test_job";
		String branch = "branchname";
		String commit = "commithash";
		String params = "branch=$BRANCH\r\ncommit=$COMMIT\r\nchoice=1;2;3";
		BitbucketVariables vars = new BitbucketVariables.Builder().branch(branch).commit(commit)
				.build();
		Job job = new Job.JobBuilder(id).jobName(jobName).buildParameters(params).build();
		Map<String, Object> actual = job.asMap(vars);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> parameterMap = (List<Map<String, Object>>) actual
				.get("buildParameters");
		assertEquals(id, actual.get("id"));
		assertEquals(jobName, actual.get("jobName"));
		assertEquals(branch, parameterMap.get(0).get("branch"));
		assertEquals(commit, parameterMap.get(1).get("commit"));
		assertArrayEquals("1;2;3".split(";"), (String[]) parameterMap.get(2).get("choice"));
	}

	@Test
	public void testBuildManualUrlNullServer() {
		Job job = new Job.JobBuilder(0).build();
		String actual = job.buildManualUrl(null, null, false);

		assertEquals(null, actual);
	}

	@Test
	public void testBuildManualUrl() {
		String jobName = "jobname";
		Server server = new Server("http://baseurl", "", "", false);
		MultivaluedMap<String, String> query = new MultivaluedMapImpl();
		Job job = new Job.JobBuilder(0).jobName(jobName).build();
		String actual = job.buildManualUrl(server, query, false);

		assertEquals(server.getBaseUrl() + "/job/" + jobName + "/build", actual);
	}

	@Test
	public void testBuildManualUrlWithParameters() {
		String jobName = "jobname";
		Server server = new Server("http://baseurl", "", "", false);
		MultivaluedMap<String, String> query = new MultivaluedMapImpl();
		query.add("param1", "value1");
		query.add("param2", "value2");
		Job job = new Job.JobBuilder(0).jobName(jobName).build();
		String actual = job.buildManualUrl(server, query, false);

		assertEquals(server.getBaseUrl() + "/job/" + jobName
				+ "/buildWithParameters?param1=value1&param2=value2", actual);
	}
}
