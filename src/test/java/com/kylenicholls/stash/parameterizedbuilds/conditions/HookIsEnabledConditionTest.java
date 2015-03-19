package com.kylenicholls.stash.parameterizedbuilds.conditions;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.stash.hook.repository.RepositoryHook;
import com.atlassian.stash.repository.Repository;
import com.kylenicholls.stash.parameterizedbuilds.conditions.HookIsEnabledCondition;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;

public class HookIsEnabledConditionTest {
	private RepositoryHook repoHook;
	private Map<String, Object> context;
	private SettingsService settingsService;
	private HookIsEnabledCondition condition;
	private Repository repository;

	@Before
	public void setup() throws Exception {
		settingsService = mock(SettingsService.class);
		repository = mock(Repository.class);
		repoHook = mock(RepositoryHook.class);

		context = new HashMap<String, Object>();
		context.put("repository", repository);

		condition = new HookIsEnabledCondition(settingsService);
	}

	@Test
	public void testShouldNotDisplayIfRepositoryNullOrNotRepository() {
		context.put("repository", null);
		assertFalse(condition.shouldDisplay(context));

		context.put("repository", "notARepository");
		assertFalse(condition.shouldDisplay(context));
	}
	
	@Test
	public void testShouldDisplayHookEnabled() {
		when(settingsService.getHook(repository)).thenReturn(repoHook);
		when(repoHook.isEnabled()).thenReturn(true);
		assertTrue(condition.shouldDisplay(context));
	}
}
