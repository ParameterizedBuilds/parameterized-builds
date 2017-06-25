package com.kylenicholls.stash.parameterizedbuilds.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.user.SecurityService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

public class SettingsServiceTest {
	private SettingsService settingsService;

	@Before
	public void setup() throws Exception {
		RepositoryHookService hookService = mock(RepositoryHookService.class);
		SecurityService securityService = mock(SecurityService.class);
		settingsService = new SettingsService(hookService, securityService);
	}

	@Test
	public void testGetJobs() {
		String jobName = "jobName";
		String jobName2 = "jobName2";
		String triggers = "add;push";
		String params = "param1=value1";
		String token = "token";
		String branchRegex = "branchRegex";
		String pathRegex = "pathRegex";
		String permissions = "permissions";
		Map<String, Object> jobConfig = new LinkedHashMap<>();
		jobConfig.put(SettingsService.JOB_PREFIX + "0", jobName);
		jobConfig.put(SettingsService.TRIGGER_PREFIX + "0", triggers);
		jobConfig.put(SettingsService.PARAM_PREFIX + "0", params);
		jobConfig.put(SettingsService.TOKEN_PREFIX + "0", token);
		jobConfig.put(SettingsService.BRANCH_PREFIX + "0", branchRegex);
		jobConfig.put(SettingsService.PATH_PREFIX + "0", pathRegex);
		jobConfig.put(SettingsService.PERMISSIONS_PREFIX + "0", permissions);
		jobConfig.put(SettingsService.JOB_PREFIX + "1", jobName2);
		jobConfig.put(SettingsService.TRIGGER_PREFIX + "1", "add");
		jobConfig.put(SettingsService.PARAM_PREFIX + "1", "");
		jobConfig.put(SettingsService.TOKEN_PREFIX + "1", "");
		jobConfig.put(SettingsService.BRANCH_PREFIX + "1", "");
		jobConfig.put(SettingsService.PATH_PREFIX + "1", "");
		jobConfig.put(SettingsService.PERMISSIONS_PREFIX + "1", "");
		List<Job> jobs = settingsService.getJobs(jobConfig);

		List<Trigger> triggerList = Arrays.asList(Trigger.ADD, Trigger.PUSH);
		Entry<String, String> paramMap = new SimpleEntry<>("param1", "value1");
		assertEquals(2, jobs.size());
		assertEquals(0, jobs.get(0).getJobId());
		assertEquals(jobName, jobs.get(0).getJobName());
		assertEquals(triggerList, jobs.get(0).getTriggers());
		assertEquals(paramMap, jobs.get(0).getBuildParameters().get(0));
		assertEquals(token, jobs.get(0).getToken());
		assertEquals(branchRegex, jobs.get(0).getBranchRegex());
		assertEquals(pathRegex, jobs.get(0).getPathRegex());
		assertEquals(1, jobs.get(1).getJobId());
		assertEquals(jobName2, jobs.get(1).getJobName());
	}

	@Test
	public void testNoJobsDefined() {
		Map<String, Object> jobConfig = new HashMap<>();
		List<Job> jobs = settingsService.getJobs(jobConfig);

		assertEquals(Collections.emptyList(), jobs);
	}

	@Test
	public void testGetJobWithUndefinedRefType() {
		Map<String, Object> jobConfig = new HashMap<>();
		jobConfig.put(SettingsService.JOB_PREFIX + "0", "jobname");
		jobConfig.put(SettingsService.TRIGGER_PREFIX + "0", "add");
		jobConfig.put(SettingsService.PARAM_PREFIX + "0", "");
		jobConfig.put(SettingsService.TOKEN_PREFIX + "0", "");
		jobConfig.put(SettingsService.BRANCH_PREFIX + "0", "");
		jobConfig.put(SettingsService.PATH_PREFIX + "0", "");
		jobConfig.put(SettingsService.PERMISSIONS_PREFIX + "0", "");
		List<Job> jobs = settingsService.getJobs(jobConfig);

		assertFalse(jobs.get(0).getIsTag());
	}

	@Test
	public void testGetJobWithTagDefined() {
		Map<String, Object> jobConfig = new HashMap<>();
		jobConfig.put(SettingsService.JOB_PREFIX + "0", "jobname");
		jobConfig.put(SettingsService.ISTAG_PREFIX + "0", "true");
		jobConfig.put(SettingsService.TRIGGER_PREFIX + "0", "add");
		jobConfig.put(SettingsService.PARAM_PREFIX + "0", "");
		jobConfig.put(SettingsService.TOKEN_PREFIX + "0", "");
		jobConfig.put(SettingsService.BRANCH_PREFIX + "0", "");
		jobConfig.put(SettingsService.PATH_PREFIX + "0", "");
		jobConfig.put(SettingsService.PERMISSIONS_PREFIX + "0", "");
		List<Job> jobs = settingsService.getJobs(jobConfig);

		assertTrue(jobs.get(0).getIsTag());
	}
}
