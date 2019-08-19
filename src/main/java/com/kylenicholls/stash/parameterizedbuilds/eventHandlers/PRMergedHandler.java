package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.commit.MinimalCommit;
import com.atlassian.bitbucket.event.pull.PullRequestMergedEvent;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.BitbucketVariables;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;

import java.util.Optional;

public class PRMergedHandler extends PRHandler{

    private String mergeCommit;

    public PRMergedHandler(SettingsService settingsService, PullRequestService pullRequestService,
                           Jenkins jenkins, PullRequestMergedEvent event, String url){
        super(settingsService, pullRequestService, jenkins, event, url, Job.Trigger.PRMERGED);
        this.mergeCommit = Optional.ofNullable(event.getCommit())
                .map(MinimalCommit::getId)
                .orElse("");
    }

    @Override
    BitbucketVariables createBitbucketVariables(){
        return new BitbucketVariables.Builder()
                .populateFromPR(pullRequest, repository, projectKey, trigger, url)
                .add("$MERGECOMMIT", () -> mergeCommit)
                .build();
    }
}
