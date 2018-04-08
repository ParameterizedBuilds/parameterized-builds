package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestParticipantApprovedEvent;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;

public class PRApprovedHandler extends PRHandler {

    public PRApprovedHandler(SettingsService settingsService, PullRequestService pullRequestService, Jenkins jenkins,
                             PullRequestParticipantApprovedEvent event, String url){
        super(settingsService, pullRequestService, jenkins, event, url, Job.Trigger.PRAPPROVED);
    }
}
