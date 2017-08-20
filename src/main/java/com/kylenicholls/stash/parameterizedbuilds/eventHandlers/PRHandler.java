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
import com.atlassian.bitbucket.repository.Branch;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.BitbucketVariables;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

import java.io.IOException;
import java.util.List;

public class PRHandler {

    private SettingsService settingsService;
    private PullRequestService pullRequestService;
    private Jenkins jenkins;
    private ApplicationUser user;
    PullRequest pullRequest;
    Repository repository;
    String projectKey;
    String url;
    final Trigger trigger;

    public PRHandler(SettingsService settingsService, PullRequestService pullRequestService, Jenkins jenkins,
                     PullRequestEvent event, String url, Trigger trigger) {
        this.settingsService = settingsService;
        this.pullRequestService = pullRequestService;
        this.jenkins = jenkins;
        this.pullRequest = event.getPullRequest();
        this.user = pullRequest.getAuthor().getUser();
        this.repository = pullRequest.getFromRef().getRepository();
        this.projectKey = repository.getProject().getKey();
        this.url = url;
        this.trigger = trigger;
    }

    public PRHandler(SettingsService settingsService, Jenkins jenkins,
                     AutomaticMergeEvent event, String url, Trigger trigger) {
        this.settingsService = settingsService;
        this.jenkins = jenkins;
        this.repository = event.getRepository();
        this.projectKey = repository.getProject().getKey();
        this.url = url;
        this.trigger = trigger;
    }

    public void run(){
        if (!settingsService.getHook(repository).isEnabled()) {
            return;
        }
        BitbucketVariables bitbucketVariables = createBitbucketVariables();
        triggerJenkinsJobs(bitbucketVariables);
    }

    BitbucketVariables createBitbucketVariables(){
        return new BitbucketVariables.Builder()
                .populateFromPR(pullRequest, repository, projectKey, trigger, url)
                .build();
    }

    String buildJenkinsUrl(BitbucketVariables bitbucketVariables, Job job){
        Server jenkinsServer = jenkins.getJenkinsServer(projectKey);
        String joinedUserToken = jenkins.getJoinedUserToken(user, projectKey);
        if (jenkinsServer == null) {
            jenkinsServer = jenkins.getJenkinsServer();
            joinedUserToken = jenkins.getJoinedUserToken(user);
        }

        return job.buildUrl(jenkinsServer, bitbucketVariables, joinedUserToken != null);
    }

    private void triggerJenkinsJobs(BitbucketVariables bitbucketVariables) {
        Settings settings = settingsService.getSettings(repository);
        if (settings == null) {
            return;
        }

        for (final Job job : settingsService.getJobs(settings.asMap())) {
            List<Trigger> triggers = job.getTriggers();
            final String pathRegex = job.getPathRegex();
            final String prDestRegex = job.getPrDestRegex();

            if (triggers.contains(trigger)) {

                String prDest = pullRequest != null ? pullRequest.getToRef().getDisplayId() : "";

                if (!(prDestRegex.trim().isEmpty() || prDest.toLowerCase().matches(prDestRegex.toLowerCase()))) {
                    return;
                }

                Server jenkinsServer = jenkins.getJenkinsServer(projectKey);
                String joinedUserToken = jenkins.getJoinedUserToken(user, projectKey);
                if (jenkinsServer == null) {
                    jenkinsServer = jenkins.getJenkinsServer();
                    joinedUserToken = jenkins.getJoinedUserToken(user);
                }

                String buildUrl = job
                        .buildUrl(jenkinsServer, bitbucketVariables, joinedUserToken != null);

                // use default user and token if the user that triggered the
                // build does not have a token set
                boolean prompt = false;
                if (joinedUserToken == null) {
                    prompt = true;
                    if (!jenkinsServer.getUser().isEmpty()) {
                        joinedUserToken = jenkinsServer.getJoinedToken();
                    }
                }

                final String token = joinedUserToken;
                final boolean finalPrompt = prompt;

                if (pathRegex.trim().isEmpty()) {
                    jenkins.triggerJob(buildUrl, token, finalPrompt);
                } else if (pullRequest != null) {
                    pullRequestService
                            .streamChanges(new PullRequestChangesRequest.Builder(pullRequest)
                                    .build(), new AbstractChangeCallback() {
                                @Override
                                public boolean onChange(Change change) throws IOException {
                                    String changedFile = change.getPath().toString();
                                    if (changedFile.matches(pathRegex)) {
                                        jenkins.triggerJob(buildUrl, token, finalPrompt);
                                        return false;
                                    }
                                    return true;
                                }

                                @Override
                                public void onEnd(ChangeSummary summary)
                                        throws IOException {
                                    // noop
                                }

                                @Override
                                public void onStart(ChangeContext context)
                                        throws IOException {
                                    // noop
                                }
                            });
                }
            }
        }
    }
}
