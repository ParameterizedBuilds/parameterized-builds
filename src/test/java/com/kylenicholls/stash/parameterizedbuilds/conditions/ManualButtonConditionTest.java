package com.kylenicholls.stash.parameterizedbuilds.conditions;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.repository.Repository;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;

public class ManualButtonConditionTest {
	private Map<String, Object> context;
	private SettingsService settingsService;
	private ManualButtonCondition condition;
	private Repository repository;

	@Before
	public void setup() throws Exception {
		settingsService = mock(SettingsService.class);
		repository = mock(Repository.class);

		context = new HashMap<String, Object>();
		context.put("repository", repository);

		condition = new ManualButtonCondition(settingsService);
	}

	@Test
	public void testShouldNotDisplayIfRepositoryNullOrNotRepository() {
		context.put("repository", null);
		assertFalse(condition.shouldDisplay(context));

		context.put("repository", "notARepository");
		assertFalse(condition.shouldDisplay(context));
	}
}
