package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestMergedEvent;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;

public class PRMergedHandler extends PRHandler{

    public PRMergedHandler(SettingsService settingsService, PullRequestService pullRequestService, Jenkins jenkins,
                           PullRequestMergedEvent event, String url){
        super(settingsService, pullRequestService, jenkins, event, url, Job.Trigger.PRMERGED);
    }
}
