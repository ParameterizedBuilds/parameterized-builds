package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.*;
import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PROpenedHandlerTest {

    private final String PR_URL = "http://pruri";
    private final Server globalServer = new Server("globalurl", "globaluser", "globaltoken", false, false);
    private SettingsService settingsService;
    private Jenkins jenkins;
    private Repository repository;
    private Job.JobBuilder jobBuilder;
    private List<Job> jobs;
    private RepositoryHook repoHook;
    private PullRequestService pullRequestService;
    private TestEventFactory eventFactory;

    @Before
    public void setup() {
        settingsService = mock(SettingsService.class);
        pullRequestService = mock(PullRequestService.class);
        jenkins = mock(Jenkins.class);
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
    public void testPROpenedAndTriggerIsPULLREQUEST() throws IOException{
        Job job = jobBuilder.triggers(new String[] { "PULLREQUEST" }).build();
        jobs.add(job);
        PullRequestOpenedEvent openedEvent = eventFactory.getMockedOpenedEvent(repository);
        PROpenedHandler handler = new PROpenedHandler(settingsService, pullRequestService, jenkins, openedEvent, PR_URL);
        handler.run();
        PROpenedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }

    @Test
    public void testPRReOpenedAndTriggerIsPULLREQUEST() throws IOException{
        Job job = jobBuilder.triggers(new String[] { "PULLREQUEST" }).build();
        jobs.add(job);
        PullRequestReopenedEvent reopenedEvent = eventFactory.getMockedReopenedEvent(repository);
        PROpenedHandler handler = new PROpenedHandler(settingsService, pullRequestService, jenkins, reopenedEvent, PR_URL);
        handler.run();
        PROpenedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }

    @Test
    public void testPRSourceRescopedAndTriggerIsPULLREQUEST() throws IOException{
        Job job = jobBuilder.triggers(new String[] { "PULLREQUEST" }).build();
        jobs.add(job);
        PullRequestRescopedEvent rescopedEvent = eventFactory.getMockedRescopedEvent(repository);
        PROpenedHandler handler = new PROpenedHandler(settingsService, pullRequestService, jenkins, rescopedEvent, PR_URL);
        PROpenedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }

}