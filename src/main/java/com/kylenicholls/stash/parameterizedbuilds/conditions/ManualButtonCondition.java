package com.kylenicholls.stash.parameterizedbuilds.conditions;

import java.util.Map;

import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.plugin.PluginParseException;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

public class ManualButtonCondition extends BaseCondition {

	public ManualButtonCondition(SettingsService settingsService) {
		super(settingsService);
	}

	@Override
	public void init(Map<String, String> context) throws PluginParseException {
		// Nothing to do here
	}

	@Override
	public boolean shouldDisplay(Map<String, Object> context) {
		final Repository repository = getRepository(context);
		if(repository == null) {
			return false;
		}
		Settings settings = settingsService.getSettings(repository);

		for (Job job : settingsService.getJobs(settings.asMap())) {
			if (job.getTriggers().contains(Trigger.MANUAL)) {
				return true;
			}
		}
		return false;
	}
}
