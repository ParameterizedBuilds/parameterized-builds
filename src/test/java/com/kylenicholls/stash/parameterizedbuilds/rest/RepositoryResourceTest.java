package com.kylenicholls.stash.parameterizedbuilds.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import javax.ws.rs.core.Response;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import static org.mockito.Mockito.when;

public class RepositoryResourceTest {
    private RepositoryResource rest;
    private AuthenticationContext authContext;
    private ApplicationUser user;
    private SettingsService settingsService;
    private Repository repo;
    private Settings settings;

    private final String USER_SLUG = "myUser";

    @Before
    public void setup() throws Exception {
        I18nService i18nService = mock(I18nService.class);
        authContext = mock(AuthenticationContext.class);
        settingsService = mock(SettingsService.class);
        rest = new RepositoryResource(i18nService, settingsService, authContext);
        user = mock(ApplicationUser.class);

        repo = mock(Repository.class);
        settings = mock(Settings.class);

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getCurrentUser()).thenReturn(user);
        when(user.getSlug()).thenReturn(USER_SLUG);
        when(settingsService.getSettings(repo)).thenReturn(settings);
    }

    private Job createDummyJob(int id){
        return new Job.JobBuilder(id).build();
    }

    @Test
    public void testGetJobsNoSettingsPresent(){
        when(settingsService.getSettings(repo)).thenReturn(null);
        Response actual = rest.getJobs(repo);

        assertEquals(Lists.newArrayList(), actual.getEntity());
    }

    @Test
    public void testGetJobsEmptySettings(){
        when(settingsService.getJobs(any())).thenReturn(Lists.newArrayList());
        Response actual = rest.getJobs(repo);

        assertEquals(Lists.newArrayList(), actual.getEntity());
    }

    @Test
    public void testGetServersOkStatus(){
        when(settingsService.getJobs(any())).thenReturn(Lists.newArrayList());
        Response actual = rest.getJobs(repo);

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testGetJobsSingleJob(){
        Job job = createDummyJob(0);
        when(settingsService.getJobs(any())).thenReturn(Lists.newArrayList(job));
        Response actual = rest.getJobs(repo);

        assertEquals(Lists.newArrayList(job.asRestMap()), actual.getEntity());
    }

    @Test
    public void testGetJobExists(){
        Job job = createDummyJob(0);
        when(settingsService.getJobs(any())).thenReturn(Lists.newArrayList(job));
        Response actual = rest.getJob(repo, 0);

        assertEquals(job.asRestMap(), actual.getEntity());
    }

    @Test
    public void testGetJobOkStatus(){
        Job job = createDummyJob(0);
        when(settingsService.getJobs(any())).thenReturn(Lists.newArrayList(job));
        Response actual = rest.getJob(repo, 0);

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testGetJobBadId(){
        String expected = "Job not found";
        Job job = createDummyJob(0);
        when(settingsService.getJobs(any())).thenReturn(Lists.newArrayList(job));
        Response actual = rest.getJob(repo, 1);

        assertEquals(expected, actual.getEntity());
    }

    @Test
    public void testGetJobBadIdStatus(){
        Job job = createDummyJob(0);
        when(settingsService.getJobs(any())).thenReturn(Lists.newArrayList(job));
        Response actual = rest.getJob(repo, 1);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), actual.getStatus());
    }
}