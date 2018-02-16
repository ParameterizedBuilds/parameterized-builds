package com.kylenicholls.stash.parameterizedbuilds;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.bitbucket.branch.automerge.AutomaticMergeEvent;
import com.atlassian.bitbucket.event.pull.*;
import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.repository.Branch;
import com.kylenicholls.stash.parameterizedbuilds.eventHandlers.BaseHandler;
import com.kylenicholls.stash.parameterizedbuilds.eventHandlers.TestEventFactory;
import org.bouncycastle.jcajce.provider.symmetric.DES;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.server.ApplicationPropertiesService;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.JobBuilder;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class PullRequestHookTest {
	private final String COMMIT = "commithash";
	private final String PR_URI = "http://pruri";
	private final Server globalServer = new Server("globalurl", "globaluser", "globaltoken", false, false);
	private SettingsService settingsService;
	private Jenkins jenkins;
	private ApplicationPropertiesService propertiesService;
	private PullRequestHook hook;
	private Repository repository;
	private JobBuilder jobBuilder;
	private List<Job> jobs;
	private RepositoryHook repoHook;
	private TestEventFactory eventFactory;
	private ExecutorService executorService;

	@Before
	public void setup() throws URISyntaxException {
		settingsService = mock(SettingsService.class);
		PullRequestService pullRequestService = mock(PullRequestService.class);
		jenkins = mock(Jenkins.class);
		propertiesService = mock(ApplicationPropertiesService.class);
		executorService = mock(ExecutorService.class);

		// executor simply invokes run on argument
		doAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            Runnable runnable = (Runnable) args[0];
            runnable.run();
            return null;
        }).when(executorService).submit(any(Runnable.class));

		when(propertiesService.getBaseUrl()).thenReturn(new URI(PR_URI));
		hook = new PullRequestHook(settingsService, pullRequestService, jenkins, propertiesService, executorService);
		eventFactory = new TestEventFactory();

		Project project = mock(Project.class);
		Settings settings = mock(Settings.class);
		repository = mock(Repository.class);
		repoHook = mock(RepositoryHook.class);

		when(repository.getProject()).thenReturn(project);
		when(settingsService.getSettings(repository)).thenReturn(settings);
		when(jenkins.getJenkinsServer()).thenReturn(globalServer);
		when(settingsService.getHook(any())).thenReturn(repoHook);
		when(repoHook.isEnabled()).thenReturn(true);

		jobBuilder = new Job.JobBuilder(1).jobName("").buildParameters("").branchRegex("")
				.pathRegex("").prDestRegex("");
		jobs = new ArrayList<>();
		when(settingsService.getJobs(any())).thenReturn(jobs);
	}

	@Test
	public void testPRSourceRescopedTriggersBuild() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PULLREQUEST" }).build();
		jobs.add(job);
		PullRequestRescopedEvent rescopedEvent = eventFactory.getMockedRescopedEvent(repository);
		when(rescopedEvent.getPreviousFromHash()).thenReturn("newhash");
		hook.onPullRequestRescoped(rescopedEvent);

		verify(jenkins, times(1))
				.triggerJob(any(), any(), any(), any());
	}

	@Test
	public void testPRDestRescopedDoesntTriggerBuild() throws IOException {
		Job job = jobBuilder.triggers(new String[] { "PULLREQUEST" }).build();
		jobs.add(job);
		PullRequestRescopedEvent rescopedEvent = eventFactory.getMockedRescopedEvent(repository);
		when(rescopedEvent.getPreviousFromHash()).thenReturn(COMMIT);
		hook.onPullRequestRescoped(rescopedEvent);

		verify(jenkins, times(0)).triggerJob(any(), any(), any(), any());
	}
}
