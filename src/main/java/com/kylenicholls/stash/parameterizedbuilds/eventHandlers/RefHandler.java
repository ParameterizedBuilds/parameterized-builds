package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.BitbucketVariables;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

public class RefHandler extends BaseHandler{

    private static final String REFS_HEADS = "refs/heads/";
    private static final String REFS_TAGS = "refs/tags/";

    final CommitService commitService;

    RefChange refChange;
    String branch;
    boolean isTag;
    Trigger trigger;
    String url;

    public RefHandler(SettingsService settingsService, Jenkins jenkins, CommitService commitService, Repository repository,
                      RefChange refChange, String url, ApplicationUser user, Trigger trigger) {
        super(settingsService, jenkins);
        this.commitService = commitService;
        this.refChange = refChange;

        branch = refChange.getRef().getId().replace(REFS_HEADS, "");
        isTag = false;
        if (refChange.getRef().getId().startsWith(REFS_TAGS)) {
            branch = refChange.getRef().getId().replace(REFS_TAGS, "");
            isTag = true;
        }

        this.repository = repository;
        this.projectKey = repository.getProject().getKey();
        this.url = url;
        this.trigger = trigger;
        this.user = user;
    }

    @Override
    BitbucketVariables createBitbucketVariables(){
        return new BitbucketVariables.Builder()
                .populateFromRef(branch, refChange, repository, projectKey, trigger, url)
                .build();
    }

    @Override
    boolean validateJob(Job job, BitbucketVariables bitbucketVariables){
        return validateTag(job, isTag) && validateBranch(job, branch) && validateTrigger(job, trigger);
    }
}
