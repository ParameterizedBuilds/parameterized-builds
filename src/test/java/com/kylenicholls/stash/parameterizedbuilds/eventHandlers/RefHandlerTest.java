package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.repository.MinimalRef;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RefHandlerTest {

    private final String BRANCH_REF = "refs/heads/branch";
    private final String PROJECT_KEY = "projectkey";
    private final String COMMIT = "commithash";
    private final String url = "http://url";
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
        jenkins = mock(Jenkins.class);

        refChange = mock(RefChange.class);
        minimalRef = mock(MinimalRef.class);
        repository = mock(Repository.class);
        project = mock(Project.class);
        user = mock(ApplicationUser.class);

        when(refChange.getRef()).thenReturn(minimalRef);
        when(refChange.getToHash()).thenReturn(COMMIT);
        when(repository.getProject()).thenReturn(project);
        when(project.getKey()).thenReturn(PROJECT_KEY);
        when(refChange.getType()).thenReturn(RefChangeType.UPDATE);

        when(minimalRef.getId()).thenReturn(BRANCH_REF);
        jobBuilder = new Job.JobBuilder(1).jobName("").buildParameters("").branchRegex("")
                .pathRegex("").ignoreComitters("").ignoreCommitMsg("");
        jobs = new ArrayList<>();
        when(settingsService.getJobs(repository)).thenReturn(jobs);
    }

    @Test
    public void testBranchRegexDoesNotMatch() {
        Job job = jobBuilder.triggers(new String[] { "push" }).branchRegex("foobar").build();
        jobs.add(job);
        PushHandler handler = new PushHandler(settingsService, jenkins, commitService, repository,
                refChange, url, user);
        PushHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(0)).triggerJenkins(any(), any());
    }

    @Test
    public void testBranchRegexEmpty() {
        Job job = jobBuilder.triggers(new String[] { "push" }).build();
        jobs.add(job);
        PushHandler handler = new PushHandler(settingsService, jenkins, commitService, repository,
                refChange, url, user);
        PushHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }

    @Test
    public void testBranchRegexMatches() {
        Job job = jobBuilder.triggers(new String[] { "push" }).branchRegex("bran.*").build();
        jobs.add(job);
        PushHandler handler = new PushHandler(settingsService, jenkins, commitService, repository,
                refChange, url, user);
        PushHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }

    @Test
    public void testBranchUpdatedAndPathRegexEmtpy() {
        Job job = jobBuilder.triggers(new String[] { "push" }).build();
        jobs.add(job);
        PushHandler handler = new PushHandler(settingsService, jenkins, commitService, repository,
                refChange, url, user);
        PushHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }

    @Test
    public void testBranchUpdatedAndTriggerIsNotPush() {
        Job job = jobBuilder.triggers(new String[] { "add" }).build();
        jobs.add(job);
        PushHandler handler = new PushHandler(settingsService, jenkins, commitService, repository,
                refChange, url, user);
        PushHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(0)).triggerJenkins(eq(job), any());
    }

    @Test
    public void testJobTriggeredWhenTagAddedAndTriggerIsTagAdded() {
        when(minimalRef.getId()).thenReturn("refs/tags/tagname");
        when(refChange.getType()).thenReturn(RefChangeType.ADD);
        Job job = jobBuilder.isTag(true).triggers(new String[] { "add" }).build();
        jobs.add(job);
        RefCreatedHandler handler = new RefCreatedHandler(settingsService, jenkins, commitService,
                repository, refChange, url, user);
        RefCreatedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }

    @Test
    public void testJobNotTriggeredWhenTagAddedAndTriggerIsBranchAdded() {
        when(minimalRef.getId()).thenReturn("refs/tags/tagname");
        when(refChange.getType()).thenReturn(RefChangeType.ADD);
        Job job = jobBuilder.isTag(false).triggers(new String[] { "add" }).build();
        jobs.add(job);
        RefCreatedHandler handler = new RefCreatedHandler(settingsService, jenkins, commitService,
                repository, refChange, url, user);
        RefCreatedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(0)).triggerJenkins(any(), any());
    }
}