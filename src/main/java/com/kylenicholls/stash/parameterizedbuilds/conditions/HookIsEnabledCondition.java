package com.kylenicholls.stash.parameterizedbuilds.conditions;

import java.util.Map;

import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;

public class HookIsEnabledCondition implements Condition {
	private static final String REPOSITORY = "repository";
	private SettingsService settingsService;

	public HookIsEnabledCondition(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	@Override
	public void init(Map<String, String> context) throws PluginParseException {
		// Nothing to do here
	}

	@Override
	public boolean shouldDisplay(Map<String, Object> context) {
		final Object obj = context.get(REPOSITORY);
		// Get current repo, if failure disable button
		if (obj == null || !(obj instanceof Repository))
			return false;

		final Repository repository = (Repository) obj;
		RepositoryHook hook = settingsService.getHook(repository);

		return hook.isEnabled();
	}
}
