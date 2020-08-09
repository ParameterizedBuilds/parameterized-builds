package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.commit.Commit;
import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.repository.MinimalRef;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.Person;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PushHandlerTest {

    private final String BRANCH_REF = "refs/heads/branch";
    private final String PROJECT_KEY = "projectkey";
    private final String COMMIT = "commithash";
    private final String url = "http://url";
    private final String commitMsg = "changes to modify conf skipCI";
    private final String committer = "admin";
    private final Server projectServer = new Server("projecturl", null, "projectuser",
            "projecttoken", false, false);
    private Settings settings;
    private RefChange refChange;
    private MinimalRef minimalRef;
    private SettingsService settingsService;
    private Jenkins jenkins;
    private CommitService commitService;
    private Repository repository;
    private Project project;
    private ApplicationUser user;
    private Job.JobBuilder jobBuilder;
    List<Job> jobs;

    @Before
    public void setup() {
        settingsService = mock(SettingsService.class);
        commitService = mock(CommitService.class);
        Commit commit = mock(Commit.class);
        jenkins = mock(Jenkins.class);
        Person person = mock(Person.class);
        settings = mock(Settings.class);
        refChange = mock(RefChange.class);
        minimalRef = mock(MinimalRef.class);
        repository = mock(Repository.class);
        project = mock(Project.class);
        user = mock(ApplicationUser.class);

        when(refChange.getRef()).thenReturn(minimalRef);
        when(refChange.getToHash()).thenReturn(COMMIT);
        when(settingsService.getSettings(any())).thenReturn(settings);
        when(repository.getProject()).thenReturn(project);
        when(project.getKey()).thenReturn(PROJECT_KEY);
        when(jenkins.getJenkinsServer(eq(project.getKey()), any())).thenReturn(projectServer);
        when(refChange.getType()).thenReturn(RefChangeType.UPDATE);

        when(minimalRef.getId()).thenReturn(BRANCH_REF);
        jobBuilder = new Job.JobBuilder(1).jobName("").buildParameters("").branchRegex("")
                .pathRegex("").ignoreCommitMsg("").ignoreComitters("");
        jobs = new ArrayList<>();
        when(settingsService.getJobs(any())).thenReturn(jobs);
        when(commitService.getCommit(any())).thenReturn(commit);
        when(commit.getMessage()).thenReturn(commitMsg);
        when(commit.getAuthor()).thenReturn(person);
        when(person.getName()).thenReturn(committer);

    }

    @Test
    public void testBranchUpdatedAndTriggerIsPush() {
        Job job = jobBuilder.triggers(new String[] { "push" }).build();
        jobs.add(job);
        PushHandler handler = new PushHandler(settingsService, jenkins, commitService, repository,
                refChange, url, user);
        PushHandler spyHandler = spy(handler);
        doNothing().when(spyHandler).triggerJenkins(any(), any());
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }

    @Test
    public void testIgnoreCommitMsgAndJobTriggerIsSkipped (){
        jobBuilder = new Job.JobBuilder(2).jobName("").buildParameters("").branchRegex("")
                .pathRegex("").ignoreCommitMsg(".*skipCI.*").ignoreComitters("");
        Job job = jobBuilder.triggers(new String[] { "push" }).build();
        jobs.add(job);
        PushHandler handler = new PushHandler(settingsService, jenkins, commitService, repository,
                refChange, url, user);
        PushHandler spyHandler = spy(handler);
        spyHandler.run();
        verify(spyHandler, times(0)).triggerJenkins(eq(job), any());
    }

    @Test
    public void testIgnoreCommitMsgAndJobIsTriggered (){
        jobBuilder = new Job.JobBuilder(2).jobName("").buildParameters("").branchRegex("")
                .pathRegex("").ignoreCommitMsg(".*kuku.*").ignoreComitters("");
        Job job = jobBuilder.triggers(new String[] { "push" }).build();
        jobs.add(job);
        PushHandler handler = new PushHandler(settingsService, jenkins, commitService, repository,
                refChange, url, user);
        PushHandler spyHandler = spy(handler);
        doNothing().when(spyHandler).triggerJenkins(any(), any());
        spyHandler.run();
        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }

    @Test
    public void testIgnoreCommittersAndJobIsTriggered (){
        jobBuilder = new Job.JobBuilder(2).jobName("").buildParameters("").branchRegex("")
                .pathRegex("").ignoreCommitMsg("").ignoreComitters("ci_user\ntest_user");
        Job job = jobBuilder.triggers(new String[] { "push" }).build();
        jobs.add(job);
        PushHandler handler = new PushHandler(settingsService, jenkins, commitService, repository,
                refChange, url, user);
        PushHandler spyHandler = spy(handler);
        doNothing().when(spyHandler).triggerJenkins(any(), any());
        spyHandler.run();
        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }

    @Test
    public void testIgnoreCommittersAndJobTriggerIsSkipped (){
        jobBuilder = new Job.JobBuilder(2).jobName("").buildParameters("").branchRegex("")
                .pathRegex("").ignoreCommitMsg("").ignoreComitters("ci_user\nadmin");
        Job job = jobBuilder.triggers(new String[] { "push" }).build();
        jobs.add(job);
        PushHandler handler = new PushHandler(settingsService, jenkins, commitService, repository,
                refChange, url, user);
        PushHandler spyHandler = spy(handler);
        doNothing().when(spyHandler).triggerJenkins(any(), any());
        spyHandler.run();
        verify(spyHandler, times(0)).triggerJenkins(eq(job), any());
    }

}