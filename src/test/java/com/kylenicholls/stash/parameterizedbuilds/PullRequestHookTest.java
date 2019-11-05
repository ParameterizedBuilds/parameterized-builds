package com.kylenicholls.stash.parameterizedbuilds;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.bitbucket.event.pull.PullRequestRescopedEvent;
import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.kylenicholls.stash.parameterizedbuilds.eventHandlers.TestEventFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.server.ApplicationPropertiesService;
import com.atlassian.bitbucket.setting.Settings;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.JenkinsConnection;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.JobBuilder;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

import java.net.URI;
import java.util.concurrent.ExecutorService;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PullRequestHook.class)
public class PullRequestHookTest {
    private final String COMMIT = "commithash";
    private final String PR_URI = "http://pruri";
    private final Server globalServer = new Server("globalurl", null, "globaluser", "globaltoken", 
            false, false);
    private SettingsService settingsService;
    private Jenkins jenkins;
    private JenkinsConnection jenkinsConn;
    private ApplicationPropertiesService propertiesService;
    private PullRequestHook hook;
    private Repository repository;
    private JobBuilder jobBuilder;
    private List<Job> jobs;
    private RepositoryHook repoHook;
    private TestEventFactory eventFactory;
    private ExecutorService executorService;

    @Before
    public void setup() throws Exception {
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
        hook = new PullRequestHook(settingsService, pullRequestService, jenkins, propertiesService, 
                executorService);
        eventFactory = new TestEventFactory();

        Project project = mock(Project.class);
        Settings settings = mock(Settings.class);
        repository = mock(Repository.class);
        repoHook = mock(RepositoryHook.class);

        when(repository.getProject()).thenReturn(project);
        when(settingsService.getSettings(repository)).thenReturn(settings);
        when(jenkins.getJenkinsServer(null)).thenReturn(globalServer);
        when(settingsService.getHook(any())).thenReturn(repoHook);
        when(repoHook.isEnabled()).thenReturn(true);

        jobBuilder = new Job.JobBuilder(1).jobName("").buildParameters("").branchRegex("")
                .pathRegex("").prDestRegex("");
        jobs = new ArrayList<>();
        when(settingsService.getJobs(any())).thenReturn(jobs);

        jenkinsConn = mock(JenkinsConnection.class);
        PowerMockito.whenNew(JenkinsConnection.class)
            .withArguments(jenkins)
            .thenReturn(jenkinsConn);
    }

    @Test
    public void testPRSourceRescopedTriggersBuild() throws IOException {
        Job job = jobBuilder.triggers(new String[] { "PRSOURCERESCOPED" }).build();
        jobs.add(job);
        PullRequestRescopedEvent rescopedEvent = eventFactory.getMockedRescopedEvent(repository);
        when(rescopedEvent.getPreviousFromHash()).thenReturn("newhash");
        hook.onPullRequestRescoped(rescopedEvent);

        verify(jenkinsConn, times(1)).triggerJob(any(), any(), any(), any());
    }

    @Test
    public void testPRDestRescopedDoesntTriggerBuild() throws IOException {
        Job job = jobBuilder.triggers(new String[] { "PRDESTRESCOPED" }).build();
        jobs.add(job);
        PullRequestRescopedEvent rescopedEvent = eventFactory.getMockedRescopedEvent(repository);
        when(rescopedEvent.getPreviousFromHash()).thenReturn(COMMIT);
        hook.onPullRequestRescoped(rescopedEvent);

        verify(jenkinsConn, times(1)).triggerJob(any(), any(), any(), any());
    }
}
