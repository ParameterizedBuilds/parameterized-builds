package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.JenkinsConnection;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.BitbucketVariables;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;

public abstract class BaseHandler {

    final SettingsService settingsService;
    final Jenkins jenkins;
    final JenkinsConnection jenkinsConn;

    //these variables are set in the subclassed handlers
    Repository repository;
    String projectKey;
    ApplicationUser user;

    public BaseHandler(SettingsService settingsService, Jenkins jenkins){
        this.settingsService = settingsService;
        this.jenkins = jenkins;
        this.jenkinsConn = new JenkinsConnection(jenkins);
    }

    public void run(){
        BitbucketVariables bitbucketVariables = createBitbucketVariables();
        Settings settings = settingsService.getSettings(repository);
        if (settings == null) {
            return;
        }

        for (final Job job : settingsService.getJobs(settings.asMap())) {
            if (validateJob(job, bitbucketVariables)) {
                triggerJenkins(job, bitbucketVariables);
            }
        }
    }

    void triggerJenkins(Job job, BitbucketVariables bitbucketVariables){
        jenkinsConn.triggerJob(projectKey, user, job, bitbucketVariables);
    }

    abstract BitbucketVariables createBitbucketVariables();

    abstract boolean validateJob(Job job, BitbucketVariables bitbucketVariables);

    boolean validateTrigger(Job job, Job.Trigger trigger){
        return job.getTriggers().contains(trigger);
    }

    boolean validateTag(Job job, boolean isTag){
        return job.getIsTag() == isTag;
    }

    boolean validateBranch(Job job, String branch){
        String branchRegex = job.getBranchRegex();
        return branchRegex.isEmpty() || branch.toLowerCase().matches(branchRegex.toLowerCase());
    }
}
