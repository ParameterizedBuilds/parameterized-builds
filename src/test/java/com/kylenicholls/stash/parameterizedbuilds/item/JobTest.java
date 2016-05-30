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
		Job job = new Job.JobBuilder(jobId).jobName(jobName).isTag(isTag).triggers(new String[] { "add", "manual" })
				.buildParameters("param1=value1\r\nparam2=value2").branchRegex(branch).pathRegex(path).token(token)
				.createJob();

		List<Trigger> triggers = new ArrayList<Trigger>();
		triggers.add(Trigger.ADD);
		triggers.add(Trigger.MANUAL);
		Map<String, String> parameters = new LinkedHashMap<String, String>();
		parameters.put("param1", "value1");
		parameters.put("param2", "value2");

		assertEquals(jobId, job.getJobId());
		assertEquals(jobName, job.getJobName());
		assertEquals(isTag, job.getIsTag());
		assertEquals(triggers, job.getTriggers());
		assertEquals(parameters, job.getBuildParameters());
		assertEquals(branch, job.getBranchRegex());
		assertEquals(path, job.getPathRegex());
		assertEquals(token, job.getToken());
	}

	@Test
	public void testCreateNewTagJob() {
		int jobId = 0;
		boolean isTag = true;
		Job job = new Job.JobBuilder(jobId).isTag(isTag).createJob();

		assertEquals(jobId, job.getJobId());
		assertEquals(isTag, job.getIsTag());
	}

	@Test
	public void testAddTriggerNullIfTriggerInvalid() {
		Job job = new Job.JobBuilder(1).triggers("".split(";")).createJob();

		assertEquals(1, job.getTriggers().size());
		assertEquals(Trigger.NULL, job.getTriggers().get(0));
	}

	@Test
	public void testBuildEmptyParameterMap() {
		Job job = new Job.JobBuilder(1).buildParameters("").createJob();

		assertEquals(new LinkedHashMap<String, String>(), job.getBuildParameters());
	}

	@Test
	public void testBuildKeyValuePairWithNullValue() {
		Job job = new Job.JobBuilder(1).buildParameters("param1=").createJob();

		Map<String, String> parameters = new LinkedHashMap<String, String>();
		parameters.put("param1", "");
		assertEquals(parameters, job.getBuildParameters());
	}

	@Test
	public void testBRANCHVariable() {
		String value = "branchname";
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder()
				.branch(value).build();
		Job job = new Job.JobBuilder(1).buildParameters("param=$BRANCH").createJob();
		
		assertEquals("param=" + value, job.getQueryString(parameters));
	}

	@Test
	public void testCOMMITVariable() {
		String value = "commit";
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder()
				.commit(value).build();
		Job job = new Job.JobBuilder(1).buildParameters("param=$COMMIT").createJob();
		
		assertEquals("param=" + value, job.getQueryString(parameters));
	}

	@Test
	public void testPRDESTINATIONVariable() {
		String value = "branchname";
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder()
				.prDestination(value).build();
		Job job = new Job.JobBuilder(1).buildParameters("param=$PRDESTINATION").createJob();
		
		assertEquals("param=" + value, job.getQueryString(parameters));
	}

	@Test
	public void testREPOSITORYVariable() {
		String value = "branchname";
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder()
				.repoName(value).build();
		Job job = new Job.JobBuilder(1).buildParameters("param=$REPOSITORY").createJob();
		
		assertEquals("param=" + value, job.getQueryString(parameters));
	}

	@Test
	public void testPROJECTVariable() {
		String value = "branchname";
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder()
				.projectName(value).build();
		Job job = new Job.JobBuilder(1).buildParameters("param=$PROJECT").createJob();
		
		assertEquals("param=" + value, job.getQueryString(parameters));
	}

	@Test
	public void testShouldUseFirstOptionForChoiceParams() {
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder().build();
		Job job = new Job.JobBuilder(1).buildParameters("param=1;2;3").createJob();
		
		assertEquals("param=1", job.getQueryString(parameters));
	}

	@Test
	public void testShouldSupportMutlipleParams() {
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder().build();
		Job job = new Job.JobBuilder(1).buildParameters("param1=value1\r\nparam2=value2").createJob();

		assertEquals("param1=value1&param2=value2", job.getQueryString(parameters));
	}
}
