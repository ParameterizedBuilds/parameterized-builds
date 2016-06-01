package com.kylenicholls.stash.parameterizedbuilds;

import static org.mockito.Matchers.any;
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

public class PullRequestHookTest {
	private SettingsService settingsService;
	private PullRequestService pullRequestService;
	private Jenkins jenkins;
	private PullRequestHook hook;
	private PullRequestOpenedEvent openedEvent;
	private PullRequestReopenedEvent reopenedEvent;
	private PullRequestRescopedEvent rescopedEvent;
	private PullRequestMergedEvent mergedEvent;
	private PullRequestDeclinedEvent declinedEvent;
	private Repository repository;
	private ApplicationUser user;

	@Before
	public void setup() {
		settingsService = mock(SettingsService.class);
		pullRequestService = mock(PullRequestService.class);
		jenkins = mock(Jenkins.class);
		hook = new PullRequestHook(settingsService, pullRequestService, jenkins);

		PullRequest pullRequest = mock(PullRequest.class);
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

		repository = mock(Repository.class);
		Project project = mock(Project.class);
		when(repository.getSlug()).thenReturn("slug");
		when(repository.getProject()).thenReturn(project);
		when(project.getName()).thenReturn("projectname");
		Settings settings = mock(Settings.class);
		PullRequestRef prFromRef = mock(PullRequestRef.class);
		PullRequestRef prToRef = mock(PullRequestRef.class);
		PullRequestParticipant author = mock(PullRequestParticipant.class);
		user = mock(ApplicationUser.class);
		when(pullRequest.getFromRef()).thenReturn(prFromRef);
		when(pullRequest.getToRef()).thenReturn(prToRef);
		when(pullRequest.getAuthor()).thenReturn(author);
		when(author.getUser()).thenReturn(user);
		when(prFromRef.getRepository()).thenReturn(repository);
		when(prFromRef.getDisplayId()).thenReturn("sourcebranch");
		when(prFromRef.getLatestCommit()).thenReturn("commithash");
		when(prToRef.getDisplayId()).thenReturn("destbranch");
		when(settingsService.getSettings(repository)).thenReturn(settings);
	}

	@Test
	public void testPROpenedAndTriggerIsPULLREQUEST() throws IOException {
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "PULLREQUEST" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		hook.onPullRequestOpened(openedEvent);
		
		verify(jenkins, times(1)).triggerJob(job, "", user, repository.getProject().getKey());
	}

	@Test
	public void testPRReOpenedAndTriggerIsPULLREQUEST() throws IOException {
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "PULLREQUEST" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		hook.onPullRequestReOpened(reopenedEvent);
		
		verify(jenkins, times(1)).triggerJob(job, "", user, repository.getProject().getKey());
	}

	@Test
	public void testPRSourceRescopedAndTriggerIsPULLREQUEST() throws IOException {
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "PULLREQUEST" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		when(rescopedEvent.getPreviousFromHash()).thenReturn("newhash");
		hook.onPullRequestRescoped(rescopedEvent);
		
		verify(jenkins, times(1)).triggerJob(job, "", user, repository.getProject().getKey());
	}

	@Test
	public void testPRDestRescopedAndTriggerIsPULLREQUEST() throws IOException {
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "PULLREQUEST" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		when(rescopedEvent.getPreviousFromHash()).thenReturn("commithash");
		hook.onPullRequestRescoped(rescopedEvent);
		
		verify(jenkins, times(0)).triggerJob(job, "", user, repository.getProject().getKey());
	}

	@Test
	public void testPRMergedAndTriggerIsPRMERGED() throws IOException {
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "PRMERGED" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		hook.onPullRequestMerged(mergedEvent);
		
		verify(jenkins, times(1)).triggerJob(job, "", user, repository.getProject().getKey());
	}

	@Test
	public void testPRDeclinedAndTriggerIsPRDECLINED() throws IOException {
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "PRDECLINED" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		hook.onPullRequestDeclined(declinedEvent);
		
		verify(jenkins, times(1)).triggerJob(job, "", user, repository.getProject().getKey());
	}

	@Test
	public void testPROpenedAndNoSettings() throws IOException {
		when(settingsService.getSettings(repository)).thenReturn(null);
		hook.onPullRequestOpened(openedEvent);
		
		verify(jenkins, times(0)).triggerJob(any(), any(), any(), any());
	}

	@Test
	public void testPROpenedAndTriggerIsPRDECLINED() throws IOException {
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "PRDECLINED" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		hook.onPullRequestOpened(openedEvent);
		
		verify(jenkins, times(0)).triggerJob(job, "", user, repository.getProject().getKey());
	}
}
