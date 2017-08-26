package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.branch.automerge.AutomaticMergeEvent;
import com.atlassian.bitbucket.event.pull.*;
import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Branch;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.server.ApplicationPropertiesService;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class PRHandlerTest {


    private final String PROJECT_KEY = "projectkey";
    private final String PROJECT_NAME = "projectname";
    private final String PR_URL = "http://pruri";
    private final Server globalServer = new Server("globalurl", "globaluser", "globaltoken", false);
    private final Server projectServer = new Server("projecturl", "projectuser", "projecttoken",
            false);
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
        when(project.getName()).thenReturn(PROJECT_NAME);
        when(project.getKey()).thenReturn(PROJECT_KEY);
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
    public void testNoSettings() throws IOException {
        Job job = jobBuilder.triggers(new String[] { "PRDECLINED" }).prDestRegex("test_branch").build();
        jobs.add(job);
        when(settingsService.getSettings(repository)).thenReturn(null);
        PullRequestDeclinedEvent declinedEvent = eventFactory.getMockedDeclinedEvent(repository);
        PRHandler handler = new PRHandler(settingsService, pullRequestService, jenkins, declinedEvent, PR_URL, Job.Trigger.PRDECLINED);
        handler.run();

        verify(jenkins, times(0)).triggerJob(any(), any(), anyBoolean());
    }

    @Test
    public void testPrDestRegexMatches() throws IOException {
        Job job = jobBuilder.triggers(new String[] { "PRDECLINED" }).prDestRegex("test_branch").build();
        jobs.add(job);
        PullRequestDeclinedEvent declinedEvent = eventFactory.getMockedDeclinedEvent(repository);
        when(declinedEvent.getPullRequest().getToRef().getDisplayId()).thenReturn("test_branch");
        PRHandler handler = new PRHandler(settingsService, pullRequestService, jenkins, declinedEvent, PR_URL, Job.Trigger.PRDECLINED);
        handler.run();

        verify(jenkins, times(1))
                .triggerJob("globalurl/job/build", globalServer.getJoinedToken(), true);
    }

    @Test
    public void testPrDestRegexDoesNotMatch() throws IOException {
        Job job = jobBuilder.triggers(new String[]{"PRDECLINED"}).prDestRegex("test_branch").build();
        jobs.add(job);
        PullRequestDeclinedEvent declinedEvent = eventFactory.getMockedDeclinedEvent(repository);
        when(declinedEvent.getPullRequest().getToRef().getDisplayId()).thenReturn("not_desired_branch");
        PRHandler handler = new PRHandler(settingsService, pullRequestService, jenkins, declinedEvent, PR_URL, Job.Trigger.PRDECLINED);
        handler.run();

        verify(jenkins, times(0)).triggerJob(any(), any(), anyBoolean());
    }

    @Test
    public void testHookIsDisabled() throws IOException {
        Job job = jobBuilder.triggers(new String[] { "PULLREQUEST" }).build();
        jobs.add(job);
        when(repoHook.isEnabled()).thenReturn(false);
        PullRequestDeclinedEvent declinedEvent = eventFactory.getMockedDeclinedEvent(repository);
        PRHandler handler = new PRHandler(settingsService, pullRequestService, jenkins, declinedEvent, PR_URL, Job.Trigger.PRDECLINED);
        handler.run();
        verify(jenkins, times(0)).triggerJob(any(),any(), anyBoolean());
    }

    @Test
    public void testMismatchingTriggers() throws IOException {
        Job job = jobBuilder.triggers(new String[] { "PRDECLINED" }).build();
        jobs.add(job);
        PullRequestOpenedEvent openedEvent = eventFactory.getMockedOpenedEvent(repository);
        PRHandler handler = new PRHandler(settingsService, pullRequestService, jenkins, openedEvent, PR_URL, Job.Trigger.PULLREQUEST);
        handler.run();

        verify(jenkins, times(0)).triggerJob(any(), any(), anyBoolean());
    }

    @Test
    public void testUseProjectServerAndUserToken() throws IOException {
        String userToken = "user:token";
        PullRequestDeclinedEvent declinedEvent = eventFactory.getMockedDeclinedEvent(repository);
        ApplicationUser user =  declinedEvent.getPullRequest().getAuthor().getUser();
        when(jenkins.getJenkinsServer(PROJECT_KEY)).thenReturn(projectServer);
        when(jenkins.getJoinedUserToken(user, PROJECT_KEY)).thenReturn(userToken);
        Job job = jobBuilder.triggers(new String[] { "PRDECLINED" }).build();
        jobs.add(job);
        PRHandler handler = new PRHandler(settingsService, pullRequestService, jenkins, declinedEvent, PR_URL, Job.Trigger.PRDECLINED);
        handler.run();

        verify(jenkins, times(1)).triggerJob("projecturl/job/build", userToken, false);
    }

    @Test
    public void testUseGlobalJenkinsAndUserToken() throws IOException {
        String userToken = "user:token";
        PullRequestDeclinedEvent declinedEvent = eventFactory.getMockedDeclinedEvent(repository);
        ApplicationUser user =  declinedEvent.getPullRequest().getAuthor().getUser();
        when(jenkins.getJenkinsServer(PROJECT_KEY)).thenReturn(null);
        when(jenkins.getJoinedUserToken(user)).thenReturn(userToken);
        Job job = jobBuilder.triggers(new String[] { "PRDECLINED" }).build();
        jobs.add(job);
        PRHandler handler = new PRHandler(settingsService, pullRequestService, jenkins, declinedEvent, PR_URL, Job.Trigger.PRDECLINED);
        handler.run();

        verify(jenkins, times(1)).triggerJob("globalurl/job/build", userToken, false);
    }

    @Test
    public void testNoDefaultUserSet() throws IOException {
        when(jenkins.getJenkinsServer()).thenReturn(new Server("buildurl", "", "", false));
        Job job = jobBuilder.triggers(new String[] { "PRDECLINED" }).build();
        jobs.add(job);
        PullRequestDeclinedEvent declinedEvent = eventFactory.getMockedDeclinedEvent(repository);
        PRHandler handler = new PRHandler(settingsService, pullRequestService, jenkins, declinedEvent, PR_URL, Job.Trigger.PRDECLINED);
        handler.run();

        verify(jenkins, times(1)).triggerJob("buildurl/job/build", null, true);
    }

}