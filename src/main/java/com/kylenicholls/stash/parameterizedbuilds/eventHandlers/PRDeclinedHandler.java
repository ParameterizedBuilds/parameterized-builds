package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestDeclinedEvent;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

public class PRDeclinedHandler extends PRHandler{

    public PRDeclinedHandler(SettingsService settingsService, PullRequestService pullRequestService,
                             Jenkins jenkins, PullRequestDeclinedEvent event, String url){
        super(settingsService, pullRequestService, jenkins, event, url, Trigger.PRDECLINED);
    }
}
