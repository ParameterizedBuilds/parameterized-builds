package com.kylenicholls.stash.parameterizedbuilds;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.hook.repository.PostRepositoryHook;
import com.atlassian.bitbucket.hook.repository.PostRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.RepositoryHookRequest;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.server.ApplicationPropertiesService;
import com.atlassian.bitbucket.setting.RepositorySettingsValidator;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsValidationErrors;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.eventHandlers.PushHandler;
import com.kylenicholls.stash.parameterizedbuilds.eventHandlers.RefCreatedHandler;
import com.kylenicholls.stash.parameterizedbuilds.eventHandlers.RefDeletedHandler;
import com.kylenicholls.stash.parameterizedbuilds.eventHandlers.RefHandler;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

public class ParameterizedBuildHook
		implements PostRepositoryHook, RepositorySettingsValidator {

	private final SettingsService settingsService;
	private final CommitService commitService;
	private final Jenkins jenkins;
	private String url;
	private ApplicationUser user;
	private ExecutorService executorService;

	public ParameterizedBuildHook(
			SettingsService settingsService,
			CommitService commitService,
			Jenkins jenkins,
			ApplicationPropertiesService applicationPropertiesService,
			AuthenticationContext actx,
			@ComponentImport
			ExecutorService executorService) {

		this.settingsService = settingsService;
		this.commitService = commitService;
		this.jenkins = jenkins;
		this.url = applicationPropertiesService.getBaseUrl().toString();
		this.user = actx.getCurrentUser();
		this.executorService = executorService;
	}

	@Override
	public void postUpdate(PostRepositoryHookContext context, RepositoryHookRequest request) {
		Collection<RefChange> refChanges = request.getRefChanges();
		Repository repository = request.getRepository();

		for (RefChange refChange : refChanges) {
			this.executorService.submit(() -> {
				RefHandler refHandler = createHandler(refChange, repository);
				refHandler.run();
			});
		}
	}

	RefHandler createHandler(RefChange refChange, Repository repository){
		switch (refChange.getType()) {
			case ADD: return new RefCreatedHandler(settingsService, jenkins, commitService, repository, refChange, url, user);
			case DELETE: return new RefDeletedHandler(settingsService, jenkins, commitService, repository, refChange, url, user);
			case UPDATE: return new PushHandler(settingsService, jenkins, commitService, repository, refChange, url, user);
			default: return new RefHandler(settingsService, jenkins, commitService, repository, refChange, url, user, Trigger.NULL);
		}
	}

	@Override
	public void validate(Settings settings, SettingsValidationErrors errors,
			Repository repository) {
		Server server = jenkins.getJenkinsServer();
		Server projectServer = jenkins.getJenkinsServer(repository.getProject().getKey());
		if ((server == null || server.getBaseUrl().isEmpty())
				&& (projectServer == null || projectServer.getBaseUrl().isEmpty())) {
			errors.addFieldError("jenkins-admin-error", "Jenkins is not setup in Bitbucket Server");
			return;
		}
		List<Job> jobList = settingsService.getJobs(settings.asMap());
		for (int i = 0; i < jobList.size(); i++) {
			Job job = jobList.get(i);
			if (job.getJobName().isEmpty()) {
				errors.addFieldError(SettingsService.JOB_PREFIX + i, "Field is required");
			}

			if (job.getTriggers().contains(Trigger.NULL)) {
				errors.addFieldError(SettingsService.TRIGGER_PREFIX
						+ i, "You must choose at least one trigger");
			}

			PatternSyntaxException branchExecption = null;
			try {
				Pattern.compile(job.getBranchRegex());
			} catch (PatternSyntaxException e) {
				branchExecption = e;
			}
			if (branchExecption != null) {
				errors.addFieldError(SettingsService.BRANCH_PREFIX + i, branchExecption
						.getDescription());
			}

			PatternSyntaxException pathExecption = null;
			try {
				Pattern.compile(job.getPathRegex());
			} catch (PatternSyntaxException e) {
				pathExecption = e;
			}
			if (pathExecption != null) {
				errors.addFieldError(SettingsService.PATH_PREFIX + i, pathExecption
						.getDescription());
			}
		}
	}
}
