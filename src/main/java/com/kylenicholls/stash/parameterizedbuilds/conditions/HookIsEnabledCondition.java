package com.kylenicholls.stash.parameterizedbuilds.conditions;

import java.util.Map;

import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;

public class HookIsEnabledCondition extends BaseCondition {

    public HookIsEnabledCondition(@ComponentImport RepositoryService repositoryService,
                                  SettingsService settingsService) {
        super(repositoryService, settingsService);
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
