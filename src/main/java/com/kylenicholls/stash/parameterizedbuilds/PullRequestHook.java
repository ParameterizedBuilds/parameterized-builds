package com.kylenicholls.stash.parameterizedbuilds;

import java.io.IOException;
import java.util.List;

import com.atlassian.bitbucket.content.AbstractChangeCallback;
import com.atlassian.bitbucket.content.Change;
import com.atlassian.bitbucket.content.ChangeContext;
import com.atlassian.bitbucket.content.ChangeSummary;
import com.atlassian.bitbucket.event.pull.PullRequestDeclinedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestMergedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestOpenedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestReopenedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestRescopedEvent;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestChangesRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.event.api.EventListener;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.GetQueryStringParameters;
import com.kylenicholls.stash.parameterizedbuilds.item.GetQueryStringParameters.Builder;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

public class PullRequestHook {
	private final SettingsService settingsService;
	private final PullRequestService pullRequestService;
	private final Jenkins jenkins;

	public PullRequestHook(SettingsService settingsService, PullRequestService pullRequestService,
			Jenkins jenkins) {
		this.settingsService = settingsService;
		this.pullRequestService = pullRequestService;
		this.jenkins = jenkins;
	}

	@EventListener
	public void onPullRequestOpened(PullRequestOpenedEvent event) throws IOException {
		PullRequest pullRequest = event.getPullRequest();
		triggerFromPR(pullRequest, Trigger.PULLREQUEST);
	}

	@EventListener
	public void onPullRequestReOpened(PullRequestReopenedEvent event) throws IOException {
		PullRequest pullRequest = event.getPullRequest();
		triggerFromPR(pullRequest, Trigger.PULLREQUEST);
	}

	@EventListener
	public void onPullRequestRescoped(PullRequestRescopedEvent event) throws IOException {
		final PullRequest pullRequest = event.getPullRequest();
		if (!event.getPreviousFromHash().equals(pullRequest.getFromRef().getLatestCommit())) {
			triggerFromPR(pullRequest, Trigger.PULLREQUEST);
		}
	}

	@EventListener
	public void onPullRequestMerged(PullRequestMergedEvent event) throws IOException {
		PullRequest pullRequest = event.getPullRequest();
		triggerFromPR(pullRequest, Trigger.PRMERGED);
	}

	@EventListener
	public void onPullRequestDeclined(PullRequestDeclinedEvent event) throws IOException {
		final PullRequest pullRequest = event.getPullRequest();
		triggerFromPR(pullRequest, Trigger.PRDECLINED);
	}

	private void triggerFromPR(PullRequest pullRequest, Trigger trigger) throws IOException {
		final Repository repository = pullRequest.getFromRef().getRepository();
		String branch = pullRequest.getFromRef().getDisplayId();
		String commit = pullRequest.getFromRef().getLatestCommit();
		String prDest = pullRequest.getToRef().getDisplayId();
		Settings settings = settingsService.getSettings(repository);
		if (settings == null) {
			return;
		}
		for (final Job job : settingsService.getJobs(settings.asMap())) {
			Builder builder = new GetQueryStringParameters.Builder();
			builder.branch(branch);
			builder.commit(commit);
			builder.prDestination(prDest);
			builder.repoName(repository.getSlug());
			builder.projectName(repository.getProject().getName());
			GetQueryStringParameters parameters = builder.build();

			final String queryParams = job.getQueryString(parameters);
			List<Trigger> triggers = job.getTriggers();
			final String pathRegex = job.getPathRegex();
			ApplicationUser user = pullRequest.getAuthor().getUser();
			if (triggers.contains(trigger)) {
				if (pathRegex.trim().isEmpty()) {
					jenkins.triggerJob(job, queryParams, user, repository.getProject().getKey());
				} else {
					pullRequestService
							.streamChanges(new PullRequestChangesRequest.Builder(pullRequest)
									.build(), new AbstractChangeCallback() {
										public boolean onChange(Change change) throws IOException {
											String changedFile = change.getPath().toString();
											if (changedFile.matches(pathRegex)) {
												jenkins.triggerJob(job, queryParams, user, repository
														.getProject().getKey());
												return false;
											}
											return true;
										}

										public void onEnd(ChangeSummary summary)
												throws IOException {
											// noop
										}

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
