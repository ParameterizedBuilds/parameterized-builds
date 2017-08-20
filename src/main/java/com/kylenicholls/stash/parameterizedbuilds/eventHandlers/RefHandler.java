package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.content.*;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
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

public class RefHandler {

    private static final String REFS_HEADS = "refs/heads/";
    private static final String REFS_TAGS = "refs/tags/";

    private final SettingsService settingsService;
    private final Jenkins jenkins;
    private final CommitService commitService;

    RefChange refChange;
    String branch;
    boolean isTag;
    Repository repository;
    String projectKey;
    Trigger trigger;
    String url;
    ApplicationUser user;

    public RefHandler(SettingsService settingsService, Jenkins jenkins, CommitService commitService, Repository repository,
                      RefChange refChange, String url, ApplicationUser user, Trigger trigger) {
        this.settingsService = settingsService;
        this.jenkins = jenkins;
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


    public void run(){
        BitbucketVariables bitbucketVariables = createBitbucketVariables();
        triggerJenkinsJobs(bitbucketVariables);
    }

    BitbucketVariables createBitbucketVariables(){
        return new BitbucketVariables.Builder()
                .populateFromRef(branch, refChange, repository, projectKey, trigger, url)
                .build();
    }

    private void triggerJenkinsJobs(BitbucketVariables bitbucketVariables) {
        Settings settings = settingsService.getSettings(repository);
        if (settings == null) {
            return;
        }

        for (Job job : settingsService.getJobs(settings.asMap())) {
            if (job.getIsTag() == isTag) {
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
                    joinedUserToken = joinUserToken(jenkinsServer, joinedUserToken);
                }

                if (buildBranchCheck(job, buildUrl, joinedUserToken, prompt)) {
                    jenkins.triggerJob(buildUrl, joinedUserToken, prompt);
                }
            }
        }
    }

    private String joinUserToken(Server jenkinsServer, String joinedUserToken) {
        if (!jenkinsServer.getUser().isEmpty()) {
            joinedUserToken = jenkinsServer.getJoinedToken();
        }
        return joinedUserToken;
    }

    private boolean buildBranchCheck(Job job, String buildUrl, String token, boolean prompt) {
        String branchRegex = job.getBranchRegex();
        String pathRegex = job.getPathRegex();
        List<Trigger> triggers = job.getTriggers();
        if (branchRegex.isEmpty() || branch.toLowerCase().matches(branchRegex.toLowerCase())) {
            if (refChange.getType() == RefChangeType.UPDATE && (triggers.contains(Trigger.PUSH))) {
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
                                jenkins.triggerJob(buildUrl, token, prompt);
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
            } else if (refChange.getType() == RefChangeType.ADD && triggers.contains(Trigger.ADD)) {
                return true;
            } else if (refChange.getType() == RefChangeType.DELETE
                    && triggers.contains(Trigger.DELETE)) {
                return true;
            }
        }
        return false;
    }
}
