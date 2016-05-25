package com.kylenicholls.stash.parameterizedbuilds.item;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.junit.Before;
import org.junit.Test;

import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

public class JobTest {
	
	@Before
	public void setup() throws Exception {
		
	}
	
	@Test
	public void testCreateNewBranchJob() {
		int jobId = 0;
		String jobName = "test_job";
		boolean isTag = false;
		List<Trigger> triggers = new ArrayList<Trigger>();
		triggers.add(Trigger.ADD);
		triggers.add(Trigger.MANUAL);
		Map<String, String> parameters = new LinkedHashMap<String, String>();
		parameters.put("param1", "value1");
		parameters.put("param2", "value2");
		String branch = "branch";
		String path = "path";
		
		Job job = new Job
				.JobBuilder(jobId)
				.jobName(jobName)
				.isTag(isTag)
				.triggers(new String[]{"add", "manual"})
				.buildParameters("param1=value1\r\nparam2=value2")
				.branchRegex(branch)
				.pathRegex(path)
				.createJob();
		
		assertEquals(jobId, job.getJobId());
		assertEquals(jobName, job.getJobName());
		assertEquals(isTag, job.getIsTag());
		assertEquals(triggers, job.getTriggers());
		assertEquals(parameters, job.getBuildParameters());
		assertEquals(branch, job.getBranchRegex());
		assertEquals(path, job.getPathRegex());
	}
	
	@Test
	public void testCreateNewTagJob() {
		int jobId = 0;
		boolean isTag = true;
		
		Job job = new Job
				.JobBuilder(jobId)
				.isTag(isTag)
				.createJob();
		
		assertEquals(jobId, job.getJobId());
		assertEquals(isTag, job.getIsTag());
	}
}
