package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

public class PushHandler extends RefHandler {

    public PushHandler(SettingsService settingsService, Jenkins jenkins, CommitService commitService,
                       Repository repository, RefChange refChange, String url, ApplicationUser user) {
        super(settingsService, jenkins, commitService, repository, refChange, url, user, Trigger.PUSH);
    }
}
