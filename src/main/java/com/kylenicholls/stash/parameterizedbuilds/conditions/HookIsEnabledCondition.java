package com.kylenicholls.stash.parameterizedbuilds.conditions;

import java.util.Map;

import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.repository.Repository;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;

public class HookIsEnabledCondition extends BaseCondition {

	public HookIsEnabledCondition(SettingsService settingsService) {
		super(settingsService);
	}

	@Override
	public void init(Map<String, String> context) {
		// Nothing to do here
	}

	@Override
	public boolean shouldDisplay(Map<String, Object> context) {
		final Repository repository = getRepository(context);
		if (repository == null) {
			return false;
		}
		RepositoryHook hook = settingsService.getHook(repository);

		return hook.isEnabled();
	}
}
