package com.kylenicholls.stash.parameterizedbuilds.conditions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;

public class ManualButtonConditionTest {
	private Map<String, Object> context;
	private SettingsService settingsService;
	private ManualButtonCondition condition;
	private Repository repository;
	private Settings settings;

	@Before
	public void setup() {
		settingsService = mock(SettingsService.class);
		repository = mock(Repository.class);
		settings = mock(Settings.class);

		context = new HashMap<>();
		context.put("repository", repository);

		condition = new ManualButtonCondition(settingsService);
	}

	@Test
	public void testShouldNotDisplayIfRepositoryNull() {
		context.put("repository", null);
		assertFalse(condition.shouldDisplay(context));
	}

	@Test
	public void testShouldNotDisplayIfNotRepository() {
		context.put("repository", "notARepository");
		assertFalse(condition.shouldDisplay(context));
	}

	@Test
	public void testShouldDisplayWhenManualTrigger() {
		when(settingsService.getSettings(repository)).thenReturn(settings);
		Job job = new Job.JobBuilder(1).jobName("").triggers("manual".split(";"))
				.buildParameters("").branchRegex("").pathRegex("").build();
		List<Job> jobs = new ArrayList<>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		assertTrue(condition.shouldDisplay(context));
	}

	@Test
	public void testShouldNotDisplayWhenNotManualTrigger() {
		when(settingsService.getSettings(repository)).thenReturn(settings);
		Job job = new Job.JobBuilder(1).jobName("").triggers("add".split(";")).buildParameters("")
				.branchRegex("").pathRegex("").build();
		List<Job> jobs = new ArrayList<>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		assertFalse(condition.shouldDisplay(context));
	}
}
