package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestDeclinedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestParticipantApprovedEvent;
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

public class PRApprovedHandlerTest {

    private final String PR_URI = "http://pruri";
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
    public void setup(){
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
    public void testPRDeclinedAndTriggerIsPRDECLINED() throws IOException{
        Job job = jobBuilder.triggers(new String[] { "PRAPPROVED" }).build();
        jobs.add(job);
        PullRequestParticipantApprovedEvent approvedEvent = eventFactory.getMockedApprovedEvent(repository);
        PRApprovedHandler handler = new PRApprovedHandler(settingsService, pullRequestService, jenkins, approvedEvent, PR_URI);
        PRApprovedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }

}