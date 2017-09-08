package com.kylenicholls.stash.parameterizedbuilds;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.atlassian.bitbucket.event.pull.*;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Branch;
import com.atlassian.bitbucket.server.ApplicationPropertiesService;
import com.atlassian.event.api.EventListener;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.eventHandlers.*;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.atlassian.bitbucket.branch.automerge.AutomaticMergeEvent;

public class PullRequestHook {
	private final SettingsService settingsService;
	private final PullRequestService pullRequestService;
	private final Jenkins jenkins;
	private final String url;
	private final ExecutorService executorService;

	public PullRequestHook(
			SettingsService settingsService,
			PullRequestService pullRequestService,
			Jenkins jenkins,
			ApplicationPropertiesService applicationPropertiesService,
			@ComponentImport
			ExecutorService executorService) {

		this.settingsService = settingsService;
		this.pullRequestService = pullRequestService;
		this.jenkins = jenkins;
		this.url = applicationPropertiesService.getBaseUrl().toString();
		this.executorService = executorService;
	}

	@EventListener
	public void onPullRequestOpened(PullRequestOpenedEvent event) throws IOException {
		runHandler(new PROpenedHandler(settingsService, pullRequestService, jenkins, event, url));
	}

	@EventListener
	public void onPullRequestReOpened(PullRequestReopenedEvent event) throws IOException {
		runHandler(new PROpenedHandler(settingsService, pullRequestService, jenkins, event, url));
	}

	@EventListener
	public void onPullRequestRescoped(PullRequestRescopedEvent event) throws IOException {
		PullRequest pullRequest = event.getPullRequest();
		// Rescoped event is triggered if the source OR destination branch is
		// updated. We only want to trigger builds if the source commit hash
		// changes
		if (!event.getPreviousFromHash().equals(pullRequest.getFromRef().getLatestCommit())) {
			runHandler(new PROpenedHandler(settingsService, pullRequestService, jenkins, event, url));
		}
	}

	@EventListener
	public void onPullRequestMerged(PullRequestMergedEvent event) throws IOException {
		runHandler(new PRMergedHandler(settingsService, pullRequestService, jenkins, event, url));
	}

	@EventListener
	public void onPullRequestAutomaticMerged(AutomaticMergeEvent event) throws IOException {
		Iterable<Branch> branches = event.getMergePath();
		for (Branch branch : branches){
			runHandler(new PRAutoMergedHandler(settingsService, jenkins, event, url, branch));
		}
	}

	@EventListener
	public void onPullRequestDeclined(PullRequestDeclinedEvent event) throws IOException {
		runHandler(new PRDeclinedHandler(settingsService, pullRequestService, jenkins, event, url));
	}

	@EventListener
	public void onPullRequestDeleted(PullRequestEvent event) throws IOException {
		try {
			Class PRDeletedEvent = Class.forName("com.atlassian.bitbucket.event.pull.PullRequestDeletedEvent");
			if (PRDeletedEvent.isInstance(event)) {
				runHandler(new PRDeletedHandler(settingsService, pullRequestService, jenkins, event, url));
			}
		} catch (ClassNotFoundException e) {
			return;
		}
	}

	protected void runHandler(BaseHandler handler) {
		this.executorService.submit(() -> handler.run());
	}
}
