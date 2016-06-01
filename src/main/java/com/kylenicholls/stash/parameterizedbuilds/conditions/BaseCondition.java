package com.kylenicholls.stash.parameterizedbuilds.conditions;

import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;

import java.util.Map;

public abstract class BaseCondition implements Condition {
    protected static final String REPOSITORY = "repository";
    protected SettingsService settingsService;

    public BaseCondition(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException {
        // Nothing to do here
    }

    public abstract boolean shouldDisplay(Map<String, Object> context);

    protected Repository getRepository(Map<String, Object> context) {
        final Object obj = context.get(REPOSITORY);
        // Get current repo, if failure disable button
        if (obj == null || !(obj instanceof Repository))
            return null;

        return (Repository) obj;
    }
}
