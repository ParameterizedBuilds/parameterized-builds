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
import com.kylenicholls.stash.parameterizedbuilds.item.BitbucketVariables;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

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
		PullRequest pullRequest = event.getPullRequest();
		// Rescoped event is triggered if the source OR destination branch is
		// updated. We only want to trigger builds if the source commit hash
		// changes
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
		PullRequest pullRequest = event.getPullRequest();
		triggerFromPR(pullRequest, Trigger.PRDECLINED);
	}

	private void triggerFromPR(PullRequest pullRequest, Trigger trigger) throws IOException {
		Repository repository = pullRequest.getFromRef().getRepository();
		ApplicationUser user = pullRequest.getAuthor().getUser();
		String projectKey = repository.getProject().getKey();
		String branch = pullRequest.getFromRef().getDisplayId();
		String commit = pullRequest.getFromRef().getLatestCommit();
		String prDest = pullRequest.getToRef().getDisplayId();
		BitbucketVariables bitbucketVariables = new BitbucketVariables.Builder().branch(branch)
				.commit(commit).prDestination(prDest).repoName(repository.getSlug())
				.projectName(repository.getProject().getName()).build();

		Settings settings = settingsService.getSettings(repository);
		if (settings == null) {
			return;
		}

		for (final Job job : settingsService.getJobs(settings.asMap())) {
			List<Trigger> triggers = job.getTriggers();
			final String pathRegex = job.getPathRegex();

			if (triggers.contains(trigger)) {
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
				} else {
					pullRequestService
							.streamChanges(new PullRequestChangesRequest.Builder(pullRequest)
									.build(), new AbstractChangeCallback() {
										public boolean onChange(Change change) throws IOException {
											String changedFile = change.getPath().toString();
											if (changedFile.matches(pathRegex)) {
												jenkins.triggerJob(buildUrl, token, finalPrompt);
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
