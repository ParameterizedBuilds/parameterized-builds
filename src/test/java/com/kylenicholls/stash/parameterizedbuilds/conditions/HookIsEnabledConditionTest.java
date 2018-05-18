package com.kylenicholls.stash.parameterizedbuilds.conditions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.bitbucket.repository.RepositoryService;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.repository.Repository;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;

import javax.servlet.http.HttpServletRequest;

public class HookIsEnabledConditionTest {
	private RepositoryHook repoHook;
	private Map<String, Object> context;
	private SettingsService settingsService;
	private RepositoryService repositoryService;
	private HookIsEnabledCondition condition;
	private Repository repository;

	@Before
	public void setup() {
		settingsService = mock(SettingsService.class);
		repositoryService = mock(RepositoryService.class);
		repository = mock(Repository.class);
		repoHook = mock(RepositoryHook.class);

		context = new HashMap<>();
		context.put("repository", repository);

		condition = new HookIsEnabledCondition(repositoryService, settingsService);
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
	public void testGetRepoFromRequest() {
		context.put("repository", null);
		HttpServletRequest mockRequest = mock(HttpServletRequest.class);
		when(mockRequest.getRequestURI()).thenReturn("/projects/PROJ1/repos/REP1/");
		when(repositoryService.getBySlug("PROJ1", "REP1")).thenReturn(repository);
		when(settingsService.getHook(repository)).thenReturn(repoHook);
		when(repoHook.isEnabled()).thenReturn(true);
		context.put("request", mockRequest);
		assertTrue(condition.shouldDisplay(context));
	}

	@Test
	public void testShouldDisplayHookEnabled() {
		when(settingsService.getHook(repository)).thenReturn(repoHook);
		when(repoHook.isEnabled()).thenReturn(true);
		assertTrue(condition.shouldDisplay(context));
	}

	@Test
	public void testShouldNotDisplayWhenHookDisable() {
		when(settingsService.getHook(repository)).thenReturn(repoHook);
		when(repoHook.isEnabled()).thenReturn(false);
		assertFalse(condition.shouldDisplay(context));
	}
}
