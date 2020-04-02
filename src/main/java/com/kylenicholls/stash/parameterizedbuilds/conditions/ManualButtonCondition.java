package com.kylenicholls.stash.parameterizedbuilds.conditions;

import java.util.Map;

import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

public class ManualButtonCondition extends BaseCondition {

    public ManualButtonCondition(@ComponentImport RepositoryService repositoryService,
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

        for (Job job : settingsService.getJobs(repository)) {
            if (job.getTriggers().contains(Trigger.MANUAL)) {
                return true;
            }
        }
        return false;
    }
}
