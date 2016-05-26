package com.kylenicholls.stash.parameterizedbuilds;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.content.AbstractChangeCallback;
import com.atlassian.bitbucket.content.Change;
import com.atlassian.bitbucket.content.ChangeContext;
import com.atlassian.bitbucket.content.ChangeSummary;
import com.atlassian.bitbucket.content.ChangesRequest;
import com.atlassian.bitbucket.hook.repository.*;
import com.atlassian.bitbucket.repository.*;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import com.atlassian.bitbucket.hook.repository.RepositoryHookContext;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.RepositorySettingsValidator;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsValidationErrors;

import java.util.Collection;
import java.util.List;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ParameterizedBuildHook implements AsyncPostReceiveRepositoryHook,
		RepositorySettingsValidator {

	private boolean pathMatched = false;
	private static final String REFS_HEADS = "refs/heads/";
	private static final String REFS_TAGS = "refs/tags/";
	
	private final SettingsService settingsService;
	private final CommitService commitService;
	private final Jenkins jenkins;
	private AuthenticationContext actx;
	
	public ParameterizedBuildHook(SettingsService settingsService,
			CommitService commitService,
			Jenkins jenkins, AuthenticationContext actx) {
		this.settingsService = settingsService;
		this.commitService = commitService;
		this.jenkins = jenkins;
		this.actx = actx;
	}

	@Override
	public void postReceive(RepositoryHookContext context,
			Collection<RefChange> refChanges) {
		Repository repository = context.getRepository();
		
		String token = jenkins.getUserToken(actx.getCurrentUser());
		
		for (RefChange refChange : refChanges) {
			String branch = refChange.getRef().getId().replace(REFS_HEADS, "");
			boolean isTag = false;
			if (refChange.getRef().getId().startsWith(REFS_TAGS)){
				branch = refChange.getRef().getId().replace(REFS_TAGS, "");
				isTag = true;
			}
			String commit = refChange.getToHash();
			
			for (Job job : settingsService.getJobs(context.getSettings().asMap())){
				pathMatched = false;
				String query = job.getQueryString(branch, commit, "");
				if (job.getIsTag() == isTag){
					if (buildBranchCheck(repository, refChange, branch, job.getBranchRegex(), job.getPathRegex(), job.getTriggers())) {
					jenkins.triggerJob(job, query, token);
					}
				}
			}
		}
	}
	
	public boolean buildBranchCheck(final Repository repository,
			RefChange refChange, String branch, String branchCheck,
			final String pathCheck, List<Trigger> triggers) {

		if (refChange.getType() == RefChangeType.UPDATE && (triggers.contains(Trigger.PUSH))) {
			if ((branchCheck.isEmpty() || branch.toLowerCase().matches(branchCheck.toLowerCase()))
					&& pathCheck.isEmpty()) {
				return true;
			} else if ((branchCheck.isEmpty() || branch.toLowerCase().matches(branchCheck.toLowerCase()))
					&& !pathCheck.isEmpty()) {
				ChangesRequest request = new ChangesRequest.Builder(repository,
						refChange.getToHash()).sinceId(refChange.getFromHash()).build();
				commitService.streamChanges(request, new AbstractChangeCallback() {
							public boolean onChange(Change change) throws IOException {
								if (change.getPath().toString().matches(pathCheck)) {
									storePathMatched(true);
									return false;
								}
								return true;
							}

							public void onStart(ChangeContext context) throws IOException {
								// noop
							}

							public void onEnd(ChangeSummary summary) throws IOException {
								// noop
							}
						});
				return pathMatched;
			}
		} else if (refChange.getType() == RefChangeType.ADD && triggers.contains(Trigger.ADD)) {
			if (branchCheck.isEmpty() || branch.toLowerCase().matches(branchCheck.toLowerCase())) {
				return true;
			}
		} else if (refChange.getType() == RefChangeType.DELETE && triggers.contains(Trigger.DELETE)){
			if (branchCheck.isEmpty() || branch.toLowerCase().matches(branchCheck.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public void storePathMatched (boolean matched){
		pathMatched = matched;
	}
	
	@Override
	public void validate(Settings settings, SettingsValidationErrors errors,
			Repository repository) {

		Server server = jenkins.getSettings();
		if (server == null || server.getBaseUrl().isEmpty()) {
			errors.addFieldError("jenkins-admin-error", "Jenkins is not setup in Bitbucket");
			return;
		}
		List<Job> jobList = settingsService.getJobs(settings.asMap());
		for (int i = 0; i < jobList.size(); i++) {
		    Job job = jobList.get(i);		    
		    if (job.getJobName().isEmpty()){
		    	errors.addFieldError(SettingsService.JOB_PREFIX + i, "Field is required");
		    }

    		if (job.getTriggers().contains(Trigger.NULL)){
		    	errors.addFieldError(SettingsService.TRIGGER_PREFIX + i, "You must choose at least one trigger");
    		}

		    PatternSyntaxException branchExecption = null;
		    try {
		        Pattern.compile(job.getBranchRegex());
		    } catch (PatternSyntaxException e) {
		    	branchExecption = e;
		    }
		    if (branchExecption != null) {
		        errors.addFieldError(SettingsService.BRANCH_PREFIX + i, branchExecption.getDescription());
		    }
		    
		    PatternSyntaxException pathExecption = null;
		    try {
		        Pattern.compile(job.getPathRegex());
		    } catch (PatternSyntaxException e) {
		    	pathExecption = e;
		    }
		    if (pathExecption != null) {
		        errors.addFieldError(SettingsService.PATH_PREFIX + i, pathExecption.getDescription());
		    }
		}
	}
}