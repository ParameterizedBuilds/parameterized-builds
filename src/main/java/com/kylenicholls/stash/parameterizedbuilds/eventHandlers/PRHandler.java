package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.branch.automerge.AutomaticMergeEvent;
import com.atlassian.bitbucket.content.AbstractChangeCallback;
import com.atlassian.bitbucket.content.Change;
import com.atlassian.bitbucket.content.ChangeContext;
import com.atlassian.bitbucket.content.ChangeSummary;
import com.atlassian.bitbucket.event.pull.PullRequestEvent;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestChangesRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.BitbucketVariables;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

import java.io.IOException;

public class PRHandler extends BaseHandler{

    private PullRequestService pullRequestService;
    PullRequest pullRequest;
    String url;
    final Trigger trigger;

    public PRHandler(SettingsService settingsService, PullRequestService pullRequestService,
                     Jenkins jenkins, PullRequestEvent event, String url, Trigger trigger) {
        super(settingsService, jenkins);
        this.pullRequestService = pullRequestService;
        this.pullRequest = event.getPullRequest();
        this.user = pullRequest.getAuthor().getUser();
        this.repository = pullRequest.getToRef().getRepository();
        this.projectKey = repository.getProject().getKey();
        this.url = url;
        this.trigger = trigger;
    }

    public PRHandler(SettingsService settingsService, Jenkins jenkins,
                     AutomaticMergeEvent event, String url, Trigger trigger) {
        super(settingsService, jenkins);
        this.repository = event.getRepository();
        this.projectKey = repository.getProject().getKey();
        this.url = url;
        this.trigger = trigger;
    }

    @Override
    public void run(){
        if (!settingsService.getHook(repository).isEnabled()) {
            return;
        }
        super.run();
    }

    @Override
    BitbucketVariables createBitbucketVariables(){
        return new BitbucketVariables.Builder()
                .populateFromPR(pullRequest, repository, projectKey, trigger, url)
                .build();
    }

    @Override
    boolean validateJob(Job job, BitbucketVariables bitbucketVariables){
        String prDest = pullRequest != null ? pullRequest.getToRef().getDisplayId() : "";
        return validatePrDest(job, prDest) && validateTrigger(job, trigger) && 
               validatePath(job, bitbucketVariables);
    }

    boolean validatePrDest(Job job,String prDest){
        String prDestRegex = job.getPrDestRegex();
        return prDestRegex.isEmpty() || prDest.toLowerCase().matches(prDestRegex.toLowerCase());
    }

    boolean validatePath(Job job, BitbucketVariables bitbucketVariables) {
        String pathRegex = job.getPathRegex();
        if (pathRegex.isEmpty()) {
            return true;
        } else if (pullRequest != null) {
            pullRequestService.streamChanges(new PullRequestChangesRequest.Builder(pullRequest)
                    .build(), new AbstractChangeCallback() {

                @Override
                public boolean onChange(Change change) throws IOException {
                    return triggerJob(change);
                }

                private boolean triggerJob(Change change) {
                    if (change.getPath().toString().matches(pathRegex)) {
                        jenkinsConn.triggerJob(projectKey, user, job, bitbucketVariables);
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
        return true;
    }
}
