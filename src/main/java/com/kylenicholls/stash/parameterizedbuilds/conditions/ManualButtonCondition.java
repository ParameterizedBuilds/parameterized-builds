package com.kylenicholls.stash.parameterizedbuilds.conditions;

import java.util.Map;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

/**
 * A Condition that passes when the webhook is enabled for the provided
 * repository.
 */
public class ManualButtonCondition implements Condition {

	private static final String REPOSITORY = "repository";

	private SettingsService settingsService;

	public ManualButtonCondition(SettingsService settingsService) {
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
		Settings settings = settingsService.getSettings(repository);

		for (Job job : settingsService.getJobs(settings.asMap())){
			if (job.getTriggers().contains(Trigger.MANUAL)){
				return true;
			}
		}
		return false;
	}
}
