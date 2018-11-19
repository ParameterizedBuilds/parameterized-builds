package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestReopenedEvent;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;

public class PRReopenedHandler extends PRHandler {
    public PRReopenedHandler(SettingsService settingsService, PullRequestService pullRequestService, Jenkins jenkins,
                             PullRequestReopenedEvent event, String url){
        super(settingsService, pullRequestService, jenkins, event, url, Job.Trigger.PRREOPENED);
    }
}
