package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestRescopedEvent;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;

public class PRDestRescopedHandler extends PRHandler {
    public PRDestRescopedHandler(SettingsService settingsService,
                                 PullRequestService pullRequestService, Jenkins jenkins,
                                 PullRequestRescopedEvent event, String url){
        super(settingsService, pullRequestService, jenkins, event, url, Job.Trigger.PRDESTRESCOPED);
    }
}
