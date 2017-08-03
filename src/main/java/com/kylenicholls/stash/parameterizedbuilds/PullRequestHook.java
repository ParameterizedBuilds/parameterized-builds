package com.kylenicholls.stash.parameterizedbuilds;

import java.io.IOException;
import java.util.List;

import com.atlassian.bitbucket.content.AbstractChangeCallback;
import com.atlassian.bitbucket.content.Change;
import com.atlassian.bitbucket.content.ChangeContext;
import com.atlassian.bitbucket.content.ChangeSummary;
import com.atlassian.bitbucket.event.pull.*;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestChangesRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Branch;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.server.ApplicationPropertiesService;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.event.api.EventListener;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.BitbucketVariables;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import com.atlassian.bitbucket.branch.automerge.AutomaticMergeEvent;

public class PullRequestHook {
	private final SettingsService settingsService;
	private final PullRequestService pullRequestService;
	private final Jenkins jenkins;
	private final ApplicationPropertiesService applicationPropertiesService;

	public PullRequestHook(SettingsService settingsService, PullRequestService pullRequestService,
			Jenkins jenkins, ApplicationPropertiesService applicationPropertiesService) {
		this.settingsService = settingsService;
		this.pullRequestService = pullRequestService;
		this.jenkins = jenkins;
		this.applicationPropertiesService = applicationPropertiesService;
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
	public void onPullRequestAutomaticMerged(AutomaticMergeEvent event) throws IOException {
		Iterable<Branch> branches = event.getMergePath();
		for (Branch branch : branches){
			triggerFromPR(branch, event, Trigger.PRAUTOMERGED);
		}
	}

	@EventListener
	public void onPullRequestDeclined(PullRequestDeclinedEvent event) throws IOException {
		PullRequest pullRequest = event.getPullRequest();
		triggerFromPR(pullRequest, Trigger.PRDECLINED);
	}

	@EventListener
	public void onPullRequestDeleted(PullRequestEvent event) throws IOException {


		try {
			Class PRDeletedEvent = Class.forName("com.atlassian.bitbucket.event.pull.PullRequestDeletedEvent");
			if (PRDeletedEvent.isInstance(event)) {
				PullRequest pullRequest = event.getPullRequest();
				triggerFromPR(pullRequest, Trigger.PRDELETED);
			}
		} catch (ClassNotFoundException e) {
			return;
		}
	}

	private void triggerFromPR(Branch branch, AutomaticMergeEvent event, Trigger trigger){
		Repository repository = event.getRepository();
		if (!settingsService.getHook(repository).isEnabled()) {
			return;
		}
		ApplicationUser user = event.getUser();
		String projectKey = repository.getProject().getKey();
		String commit = branch.getLatestCommit();
		String branch_name = branch.getDisplayId();
		String url = applicationPropertiesService.getBaseUrl().toString();
		BitbucketVariables.Builder builder = new BitbucketVariables.Builder().branch(branch_name)
				.commit(commit).url(url).trigger(trigger)
				.repoName(repository.getSlug())
				.projectName(projectKey);

		BitbucketVariables bitbucketVariables = builder.build();
		triggerJenkinsJobs(bitbucketVariables, repository, trigger, projectKey, user, null);
	}

	private void triggerFromPR(PullRequest pullRequest, Trigger trigger) throws IOException {
		Repository repository = pullRequest.getFromRef().getRepository();
		if (!settingsService.getHook(repository).isEnabled()) {
			return;
		}
		ApplicationUser user = pullRequest.getAuthor().getUser();
		String projectKey = repository.getProject().getKey();
		String branch = pullRequest.getFromRef().getDisplayId();
		String commit = pullRequest.getFromRef().getLatestCommit();
		String url = applicationPropertiesService.getBaseUrl().toString();
		long prId = pullRequest.getId();
		String prAuthor = pullRequest.getAuthor().getUser().getDisplayName();
		String prTitle = pullRequest.getTitle();
		String prDescription = pullRequest.getDescription();
		String prDest = pullRequest.getToRef().getDisplayId();
		String prUrl = url + "/projects/" + projectKey + "/repos/" + repository.getSlug() + "/pull-requests/" + prId;
		BitbucketVariables.Builder builder = new BitbucketVariables.Builder().branch(branch)
				.commit(commit).url(url).prId(prId).prAuthor(prAuthor).prTitle(prTitle)
				.prDestination(prDest).prUrl(prUrl).trigger(trigger)
				.repoName(repository.getSlug())
				.projectName(projectKey);
		if (prDescription != null) {
			builder.prDescription(prDescription);
		}
		BitbucketVariables bitbucketVariables = builder.build();
		triggerJenkinsJobs(bitbucketVariables, repository, trigger, projectKey, user, pullRequest);
	}

	private void triggerJenkinsJobs(BitbucketVariables bitbucketVariables, Repository repository, Trigger trigger, String projectKey, ApplicationUser user, PullRequest pullRequest){
		Settings settings = settingsService.getSettings(repository);
		if (settings == null) {
			return;
		}

		for (final Job job : settingsService.getJobs(settings.asMap())) {
			List<Trigger> triggers = job.getTriggers();
			final String pathRegex = job.getPathRegex();
			final String prDestRegex = job.getPrDestRegex();

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
				String prDest = pullRequest != null ? pullRequest.getToRef().getDisplayId() : "";

				if (!(prDestRegex.trim().isEmpty() || prDest.toLowerCase().matches(prDestRegex.toLowerCase()))) {
					return;
				}

				if (pathRegex.trim().isEmpty()) {
					jenkins.triggerJob(buildUrl, token, finalPrompt);
				} else if (pullRequest != null){
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
