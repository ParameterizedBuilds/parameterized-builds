package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.commit.CommitRequest;
import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.content.AbstractChangeCallback;
import com.atlassian.bitbucket.content.Change;
import com.atlassian.bitbucket.content.ChangeContext;
import com.atlassian.bitbucket.content.ChangeSummary;
import com.atlassian.bitbucket.content.ChangesRequest;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.BitbucketVariables;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;
import java.io.IOException;
import java.util.Arrays;

public class PushHandler extends RefHandler {

    public PushHandler(SettingsService settingsService, Jenkins jenkins,
                       CommitService commitService, Repository repository, RefChange refChange,
                       String url, ApplicationUser user) {
        super(settingsService, jenkins, commitService, repository, refChange, url, user,
              Trigger.PUSH);
    }

    @Override
    boolean validateJob(Job job, BitbucketVariables bitbucketVariables) {
        return super.validateJob(job, bitbucketVariables) && validatePath(job, bitbucketVariables)
                && validateCommitMsg(job) && validateComitter(job);
    }

    boolean validatePath(Job job, BitbucketVariables bitbucketVariables) {
        String pathRegex = job.getPathRegex();
        if (pathRegex.isEmpty()) {
            return true;
        } else {
            ChangesRequest request = new ChangesRequest.Builder(repository,
                    refChange.getToHash()).sinceId(refChange.getFromHash()).build();
            commitService.streamChanges(request, new AbstractChangeCallback() {

                @Override
                public boolean onChange(Change change) throws IOException {
                    return triggerJob(change);
                }

                private boolean triggerJob(Change change) {
                    if (change.getPath().toString().matches(pathRegex)) {
                        jenkins.triggerJob(projectKey, user, job, bitbucketVariables);
                        return false;
                    }
                    return true;
                }

                @Override
                public void onStart(ChangeContext context) throws IOException {
                    // noop
                }

                @Override
                public void onEnd(ChangeSummary summary) throws IOException {
                    // noop
                }
            });
            return false;
        }
    }

    boolean validateCommitMsg(Job job) {
        String ignoreCommitMsg = job.getIgnoreCommitMsg();
        if(ignoreCommitMsg.isEmpty()){
            return true;
        }
        else{
            CommitRequest commitRequest = new CommitRequest.Builder(
                    repository, refChange.getToHash()).build();
            String actualCommitMsg = commitService.getCommit(commitRequest).getMessage();
            boolean hasIgnoreMsg = actualCommitMsg != null &&
                    actualCommitMsg.toLowerCase().contains(ignoreCommitMsg.toLowerCase());
            return !hasIgnoreMsg;
        }
    }

    boolean validateComitter(Job job){
        String ignoreComitters = job.getIgnoreComitters();
        if(ignoreComitters.isEmpty()){
            return true;
        }
        else{
            String[] ignoreComitterList = job.getIgnoreComitters().toLowerCase().split(",");
            CommitRequest commitRequest = new CommitRequest.Builder(
                    repository, refChange.getToHash()).build();
            String author = commitService.getCommit(commitRequest).getAuthor().getName();
            boolean authorIgnored = Arrays.stream(ignoreComitterList).anyMatch(t ->
                    t.equals(author.toLowerCase()));
            return !authorIgnored;
        }
    }
}
