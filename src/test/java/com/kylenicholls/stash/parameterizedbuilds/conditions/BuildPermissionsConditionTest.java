package com.kylenicholls.stash.parameterizedbuilds.conditions;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BuildPermissionsConditionTest {

    private BuildPermissionsCondition condition;
    private Repository repository;
    private Map<String, Object> context;
    private SettingsService settingsService;
    private RepositoryService repositoryService;

    @Before
    public void setup() {
        repository = mock(Repository.class);
        settingsService = mock(SettingsService.class);
        repositoryService = mock(RepositoryService.class);

        context = new HashMap<>();
        context.put("repository", repository);

        PermissionService permissionService = mock(PermissionService.class);
        AuthenticationContext authContext = mock(AuthenticationContext.class);
        condition = new BuildPermissionsCondition(repositoryService, permissionService,
                settingsService, authContext);

        when(permissionService.hasRepositoryPermission(any(), any(), eq(Permission.REPO_WRITE)))
                .thenReturn(true);
        when(permissionService.hasRepositoryPermission(any(), any(), eq(Permission.REPO_READ)))
                .thenReturn(true);
    }

    @Test
    public void testShouldNotDisplayIfRepositoryNull() {
        context.put("repository", null);
        assertFalse(condition.shouldDisplay(context));
    }

    @Test
    public void testShouldNotDisplayIfNotRepository() {
        context.put("repository", "notARepository");
        assertFalse(condition.shouldDisplay(context));
    }

    @Test
    public void testGetRepoFromRequest() {
        context.put("repository", null);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/projects/PROJ1/repos/REP1/");
        when(repositoryService.getBySlug("PROJ1", "REP1")).thenReturn(repository);
        Job job = new Job.JobBuilder(1).permissions("REPO_WRITE").build();
        List<Job> jobs = new ArrayList<>();
        jobs.add(job);
        when(settingsService.getJobs(repository)).thenReturn(jobs);
        context.put("request", mockRequest);
        assertTrue(condition.shouldDisplay(context));
    }

    @Test
    public void testShouldNotDisplayIfInsufficientPermissions() {
        Job job = new Job.JobBuilder(1).permissions("REPO_ADMIN").build();
        List<Job> jobs = new ArrayList<>();
        jobs.add(job);
        when(settingsService.getJobs(repository)).thenReturn(jobs);
        assertFalse(condition.shouldDisplay(context));
    }

    @Test
    public void testShouldDisplayExplicitPermissionPresent() {
        Job job = new Job.JobBuilder(1).permissions("REPO_WRITE").build();
        List<Job> jobs = new ArrayList<>();
        jobs.add(job);
        when(settingsService.getJobs(repository)).thenReturn(jobs);
        assertTrue(condition.shouldDisplay(context));
    }

    @Test
    public void testShouldDisplayImplicitPermissionPresent() {
        Job job = new Job.JobBuilder(1).permissions("REPO_READ").build();
        List<Job> jobs = new ArrayList<>();
        jobs.add(job);
        when(settingsService.getJobs(repository)).thenReturn(jobs);
        assertTrue(condition.shouldDisplay(context));
    }

}