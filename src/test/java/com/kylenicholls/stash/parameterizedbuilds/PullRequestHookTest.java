package com.kylenicholls.stash.parameterizedbuilds;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.event.pull.PullRequestDeclinedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestMergedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestOpenedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestReopenedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestRescopedEvent;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.JobBuilder;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

public class PullRequestHookTest {
	private final String PROJECT_KEY = "projectkey";
	private final String SOURCE_BRANCH = "sourcebranch";
	private final String DEST_BRANCH = "destbranch";
	private final String COMMIT = "commithash";
	private final String REPO_SLUG = "reposlug";
	private final String PROJECT_NAME = "projectname";
	private final Server globalServer = new Server("globalurl", "globaluser", "globaltoken", false);
	private final Server projectServer = new Server("projecturl", "projectuser", "projecttoken",
			false);
	private SettingsService settingsService;
	private Jenkins jenkins;
	private PullRequestHook hook;
	private PullRequestOpenedEvent openedEvent;
	private PullRequestReopenedEvent reopenedEvent;
	private PullRequestRescopedEvent rescopedEvent;
	private PullRequestMergedEvent mergedEvent;
	private PullRequestDeclinedEvent declinedEvent;
	private Repository repository;
	private ApplicationUser user;
	private JobBuilder jobBuilder;
	private List<Job> jobs;

	@Before
	public void setup() {
		settingsService = mock(SettingsService.class);
		PullRequestService pullRequestService = mock(PullRequestService.class);
		jenkins = mock(Jenkins.class);
		hook = new PullRequestHook(settingsService, pullRequestService, jenkins);

		PullRequest pullRequest = mock(PullRequest.class);
		Project project = mock(Project.class);
		Settings settings = mock(Settings.class);
		PullRequestRef prFromRef = mock(PullRequestRef.class);
		PullRequestRef prToRef = mock(PullRequestRef.class);
		PullRequestParticipant author = mock(PullRequestParticipant.class);
		user = mock(ApplicationUser.class);
		repository = mock(Repository.class);
		openedEvent = mock(PullRequestOpenedEvent.class);
		reopenedEvent = mock(PullRequestReopenedEvent.class);
		rescopedEvent = mock(PullRequestRescopedEvent.class);
		mergedEvent = mock(PullRequestMergedEvent.class);
		declinedEvent = mock(PullRequestDeclinedEvent.class);

		when(openedEvent.getPullRequest()).thenReturn(pullRequest);
		when(reopenedEvent.getPullRequest()).thenReturn(pullRequest);
		when(rescopedEvent.getPullRequest()).thenReturn(pullRequest);
		when(mergedEvent.getPullRequest()).thenReturn(pullRequest);
		when(declinedEvent.getPullRequest()).thenReturn(pullRequest);
		when(repository.getSlug()).thenReturn(REPO_SLUG);
		when(repository.getProject()).thenReturn(project);
		when(project.getName()).thenReturn(PROJECT_NAME);
		when(project.getKey()).thenReturn(PROJECT_KEY);
		when(pullRequest.getFromRef()).thenReturn(prFromRef);
		when(pullRequest.getToRef()).thenReturn(prToRef);
		when(pullRequest.getAuthor()).thenReturn(author);
		when(author.getUser()).thenReturn(user);
		when(prFromRef.getRepository()).thenReturn(repository);
		when(prFromRef.getDisplayId()).thenReturn(SOURCE_BRANCH);
		when(prFromRef.getLatestCommit()).thenReturn(COMMIT);
		when(prToRef.getDisplayId()).thenReturn(DEST_BRANCH);
		when(settingsService.getSettings(repository)).thenReturn(settings);
		when(jenkins.getJenkinsServer()).thenReturn(globalServer);

		jobBuilder = new Job.JobBuilder(1).jobName("").buildParameters("").branchRegex("")
				.pathRegex("");
		jobs = new ArrayList<>();
		when(settingsService.getJobs(any())).thenReturn(jobs);
	}

	@Test
	public void testPROpenedAndTriggerIsPULLREQUEST() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PULLREQUEST" }).build();
		jobs.add(job);
		hook.onPullRequestOpened(openedEvent);
		verify(jenkins, times(1))
				.triggerJob("globalurl/job/build", globalServer.getJoinedToken(), true);
	}

	@Test
	public void testPRReOpenedAndTriggerIsPULLREQUEST() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PULLREQUEST" }).build();
		jobs.add(job);
		hook.onPullRequestReOpened(reopenedEvent);

		verify(jenkins, times(1))
				.triggerJob("globalurl/job/build", globalServer.getJoinedToken(), true);
	}

	@Test
	public void testPRSourceRescopedAndTriggerIsPULLREQUEST() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PULLREQUEST" }).build();
		jobs.add(job);
		when(rescopedEvent.getPreviousFromHash()).thenReturn("newhash");
		hook.onPullRequestRescoped(rescopedEvent);

		verify(jenkins, times(1))
				.triggerJob("globalurl/job/build", globalServer.getJoinedToken(), true);
	}

	@Test
	public void testPRDestRescopedAndTriggerIsPULLREQUEST() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PULLREQUEST" }).build();
		jobs.add(job);
		when(rescopedEvent.getPreviousFromHash()).thenReturn(COMMIT);
		hook.onPullRequestRescoped(rescopedEvent);

		verify(jenkins, times(0)).triggerJob(any(), any(), anyBoolean());
	}

	@Test
	public void testPRMergedAndTriggerIsPRMERGED() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PRMERGED" }).build();
		jobs.add(job);
		hook.onPullRequestMerged(mergedEvent);

		verify(jenkins, times(1))
				.triggerJob("globalurl/job/build", globalServer.getJoinedToken(), true);
	}

	@Test
	public void testPRDeclinedAndTriggerIsPRDECLINED() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PRDECLINED" }).build();
		jobs.add(job);
		hook.onPullRequestDeclined(declinedEvent);

		verify(jenkins, times(1))
				.triggerJob("globalurl/job/build", globalServer.getJoinedToken(), true);
	}

	@Test
	public void testPROpenedAndNoSettings() throws IOException {
		when(settingsService.getSettings(repository)).thenReturn(null);
		hook.onPullRequestOpened(openedEvent);

		verify(jenkins, times(0)).triggerJob(any(), any(), anyBoolean());
	}

	@Test
	public void testPROpenedAndTriggerIsPRDECLINED() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PRDECLINED" }).build();
		jobs.add(job);
		hook.onPullRequestOpened(openedEvent);

		verify(jenkins, times(0)).triggerJob(any(), any(), anyBoolean());
	}

	@Test
	public void testUseProjectServerAndUserToken() throws IOException {
		String userToken = "user:token";
		when(jenkins.getJenkinsServer(PROJECT_KEY)).thenReturn(projectServer);
		when(jenkins.getJoinedUserToken(user, PROJECT_KEY)).thenReturn(userToken);
		Job job = jobBuilder.triggers(new String[] { "PRDECLINED" }).build();
		jobs.add(job);
		hook.onPullRequestDeclined(declinedEvent);

		verify(jenkins, times(1)).triggerJob("projecturl/job/build", userToken, false);
	}

	@Test
	public void testUseGlobalJenkinsAndUserToken() throws IOException {
		String userToken = "user:token";
		when(jenkins.getJenkinsServer(PROJECT_KEY)).thenReturn(null);
		when(jenkins.getJoinedUserToken(user)).thenReturn(userToken);
		Job job = jobBuilder.triggers(new String[] { "PRDECLINED" }).build();
		jobs.add(job);
		hook.onPullRequestDeclined(declinedEvent);

		verify(jenkins, times(1)).triggerJob("globalurl/job/build", userToken, false);
	}

	@Test
	public void testNoDefaultUserSet() throws IOException {
		when(jenkins.getJenkinsServer()).thenReturn(new Server("buildurl", "", "", false));
		Job job = jobBuilder.triggers(new String[] { "PRDECLINED" }).build();
		jobs.add(job);
		hook.onPullRequestDeclined(declinedEvent);

		verify(jenkins, times(1)).triggerJob("buildurl/job/build", null, true);
	}

	@Test
	public void testSourceBranchVariable() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PRDECLINED" })
				.buildParameters("param=$BRANCH").build();
		jobs.add(job);
		hook.onPullRequestDeclined(declinedEvent);

		verify(jenkins, times(1)).triggerJob("globalurl/job/buildWithParameters?param="
				+ SOURCE_BRANCH, globalServer.getJoinedToken(), true);
	}

	@Test
	public void testCommitVariable() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PRDECLINED" })
				.buildParameters("param=$COMMIT").build();
		jobs.add(job);
		hook.onPullRequestDeclined(declinedEvent);

		verify(jenkins, times(1)).triggerJob("globalurl/job/buildWithParameters?param="
				+ COMMIT, globalServer.getJoinedToken(), true);
	}

	@Test
	public void testDestBranchVariable() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PRDECLINED" })
				.buildParameters("param=$PRDESTINATION").build();
		jobs.add(job);
		hook.onPullRequestDeclined(declinedEvent);

		verify(jenkins, times(1)).triggerJob("globalurl/job/buildWithParameters?param="
				+ DEST_BRANCH, globalServer.getJoinedToken(), true);
	}

	@Test
	public void testRepoNameVariable() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PRDECLINED" })
				.buildParameters("param=$REPOSITORY").build();
		jobs.add(job);
		hook.onPullRequestDeclined(declinedEvent);

		verify(jenkins, times(1)).triggerJob("globalurl/job/buildWithParameters?param="
				+ REPO_SLUG, globalServer.getJoinedToken(), true);
	}

	@Test
	public void testProjectNameVariable() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PRDECLINED" })
				.buildParameters("param=$PROJECT").build();
		jobs.add(job);
		hook.onPullRequestDeclined(declinedEvent);

		verify(jenkins, times(1)).triggerJob("globalurl/job/buildWithParameters?param="
				+ PROJECT_NAME, globalServer.getJoinedToken(), true);
	}
}
