package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestEvent;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

public class PRDeletedHandler extends PRHandler{

    public PRDeletedHandler(SettingsService settingsService, PullRequestService pullRequestService,
                            Jenkins jenkins, PullRequestEvent event, String url){
        super(settingsService, pullRequestService, jenkins, event, url, Trigger.PRDELETED);
    }
}
