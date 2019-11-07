package com.kylenicholls.stash.parameterizedbuilds.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.server.ApplicationPropertiesService;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.JenkinsConnection;
import com.kylenicholls.stash.parameterizedbuilds.conditions.BuildPermissionsCondition;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.JenkinsResponse;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BuildResource.class)
@PowerMockIgnore("javax.security.*")
public class BuildResourceTest {
    private final String REPO_SLUG = "reposlug";
    private final String PROJECT_KEY = "projkey";
    private final String URI = "http://uri";
    private BuildResource rest;
    private Jenkins jenkins;
    private JenkinsConnection jenkinsConn;
    private Repository repository;
    private AuthenticationContext authContext;
    private SettingsService settingsService;
    private ApplicationPropertiesService propertiesService;
    private PullRequestService prService;
    private BuildPermissionsCondition permissionsCheck;
    private Settings settings;
    private UriInfo uriInfo;
    private ApplicationUser user;
    private List<Job> jobs;
    private RepositoryHook hook;
    private final Server globalServer = new Server("globalurl", "global server", "globaluser",
            "globaltoken", false, false);
    private final List<Server> globalServers = Lists.newArrayList(globalServer);
    private final Server projectServer = new Server("projecturl", "project server", "projectuser",
            "projecttoken", false, false);
    private final List<Server> projectServers = Lists.newArrayList(projectServer);

    @Before
    public void setup() throws Exception {
        I18nService i18nService = mock(I18nService.class);
        settingsService = mock(SettingsService.class);
        jenkins = mock(Jenkins.class);
        authContext = mock(AuthenticationContext.class);
        propertiesService = mock(ApplicationPropertiesService.class);
        prService = mock(PullRequestService.class);
        permissionsCheck = mock(BuildPermissionsCondition.class);
        rest = new BuildResource(i18nService, settingsService, jenkins, propertiesService,
                prService, authContext, permissionsCheck);

        repository = mock(Repository.class);
        settings = mock(Settings.class);
        uriInfo = mock(UriInfo.class);
        Project project = mock(Project.class);

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getCurrentUser()).thenReturn(user);
        when(settingsService.getSettings(repository)).thenReturn(settings);
        when(repository.getProject()).thenReturn(project);
        when(jenkins.getJenkinsServers(null)).thenReturn(globalServers);
        when(repository.getSlug()).thenReturn(REPO_SLUG);
        when(project.getKey()).thenReturn(PROJECT_KEY);
        when(propertiesService.getBaseUrl()).thenReturn(new URI(URI));
        when(permissionsCheck.checkPermissions(any(), any(), any())).thenReturn(true);

        jobs = new ArrayList<>();
        when(settingsService.getJobs(any())).thenReturn(jobs);
        hook = mock(RepositoryHook.class);
        when(settingsService.getHook(any())).thenReturn(hook);
        
        jenkinsConn = mock(JenkinsConnection.class);
        PowerMockito.whenNew(JenkinsConnection.class)
            .withArguments(jenkins)
            .thenReturn(jenkinsConn);
    }

    @Test
    public void testTriggerBuildNotAuthed() {
        when(authContext.isAuthenticated()).thenReturn(false);
        Response actual = rest.triggerBuild(repository, null, null, null);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testTriggerBuildNoRepoSettings() {
        when(settingsService.getSettings(repository)).thenReturn(null);
        Response actual = rest.triggerBuild(repository, null, null,null);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testTriggerBuildNoMatchingJob() {
        Job job = new Job.JobBuilder(1).triggers(new String[] { "add" }).build();
        jobs.add(job);
        Response actual = rest.triggerBuild(repository, "0", "test",null);

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("message", "No settings found for this job");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), actual.getStatus());
        assertEquals(expected, actual.getEntity());
    }

    @Test
    public void testTriggerBuildWithQueryParams() {
        JenkinsResponse message = new JenkinsResponse.JenkinsMessage().error(false).build();
        Job job = new Job.JobBuilder(0).jobName("job").build();
        jobs.add(job);
        MultivaluedMap<String, String> query = new MultivaluedMapImpl();
        query.add("param1", "value1");
        query.add("param2", "value2");
        when(uriInfo.getQueryParameters()).thenReturn(query);
        when(jenkinsConn.triggerJob(any(), any(), any(), any())).thenReturn(message);
        Response results = rest.triggerBuild(repository, "0", "test", uriInfo);

        assertEquals(Response.Status.OK.getStatusCode(), results.getStatus());
        assertEquals(message.getMessage(), results.getEntity());
    }

    @Test
    public void testGetJenkinsServersNotAuthed() {
        when(authContext.isAuthenticated()).thenReturn(false);
        Response actual = rest.getJenkinsServers(repository);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testGetJenkinsServersOnlyProjectDefined() {
        when(jenkins.getJenkinsServers(null)).thenReturn(Lists.newArrayList());
        when(jenkins.getJenkinsServers(PROJECT_KEY)).thenReturn(projectServers);
        Response actual = rest.getJenkinsServers(repository);

        @SuppressWarnings("serial")
        Map<String, String> expected = new HashMap<String, String>() {{
            put("url", projectServer.getBaseUrl());
            put("alias", projectServer.getAlias());
            put("scope", "project");
            put("project", PROJECT_KEY);
            put("default_user", projectServer.getUser());
        }};

        assertEquals(Collections.singletonList(expected), actual.getEntity());
    }

    @Test
    public void testGetJenkinsServersOnlyGlobalDefined() {
        Response actual = rest.getJenkinsServers(repository);

        @SuppressWarnings("serial")
        Map<String, String> expected = new HashMap<String, String>() {{
            put("url", globalServer.getBaseUrl());
            put("alias", globalServer.getAlias());
            put("scope", "global");
            put("project", null);
            put("default_user", globalServer.getUser());
        }};

        assertEquals(Collections.singletonList(expected), actual.getEntity());
    }

    @Test
    public void testGetJenkinsServersProjectAndGlobalDefined() {
        when(jenkins.getJenkinsServers(PROJECT_KEY)).thenReturn(projectServers);
        Response actual = rest.getJenkinsServers(repository);

        @SuppressWarnings("serial")
        Map<String, String> expectedProject = new HashMap<String, String>() {{
            put("url", projectServer.getBaseUrl());
            put("alias", projectServer.getAlias());
            put("scope", "project");
            put("project", PROJECT_KEY);
            put("default_user", projectServer.getUser());
        }};

        @SuppressWarnings("serial")
        Map<String, String> expectedGlobal = new HashMap<String, String>() {{
            put("url", globalServer.getBaseUrl());
            put("alias", globalServer.getAlias());
            put("scope", "global");
            put("scope", "global");
            put("project", null);
            put("default_user", globalServer.getUser());
        }};

        assertEquals(Lists.newArrayList(expectedGlobal, expectedProject), actual.getEntity());
    }

    @Test
    public void testGetJenkinsServersNoServersDefined() {
        when(jenkins.getJenkinsServers(null)).thenReturn(Lists.newArrayList());
        Response actual = rest.getJenkinsServers(repository);

        assertEquals(Lists.newArrayList(), actual.getEntity());
    }

    @Test
    public void testGetJobsNotAuthed() {
        when(authContext.isAuthenticated()).thenReturn(false);
        Response actual = rest.getJobs(repository, "branch", "commit", null, 0);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testGetJobsNoRepoSettings() {
        when(settingsService.getSettings(repository)).thenReturn(null);
        Response actual = rest.getJobs(repository, "branch", "commit", null, 0);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), actual.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetJobsNoManualJob() {
        Job job = new Job.JobBuilder(1).triggers(new String[] { "add" }).build();
        jobs.add(job);
        Response actual = rest.getJobs(repository, "branch", "commit", null, 0);

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
        assertEquals(new ArrayList<Map<String, Object>>(), (List<Map<String, Object>>) actual
                .getEntity());
    }

    @Test
    public void testGetJobsWithPR() {
        int jobId = 1;
        String jobName = "jobname";
        long prId = 101L;
        String authorName = "prauthorname";
        String authorEmail = "user@example.com";
        String title = "prtitle";
        String description = "prdescription";
        String prDest = "prbranch";
        Job job = new Job.JobBuilder(1).jobName(jobName).triggers(new String[] { "manual" })
                .buildParameters("param1=$BRANCH\r\nparam2=$PRDESTINATION\r\nparam3=$PRURL\r\n" +
                                 "param4=$PRAUTHOR\r\nparam5=$PREMAIL\r\nparam6=$PRTITLE\r\n" +
                                 "param7=$PRDESCRIPTION\r\nparam8=$PRID")
                .permissions("REPO_ADMIN").build();
        jobs.add(job);
        PullRequest pr = mock(PullRequest.class);
        PullRequestParticipant author = mock(PullRequestParticipant.class);
        when(pr.getAuthor()).thenReturn(author);
        ApplicationUser prUser = mock(ApplicationUser.class);
        when(author.getUser()).thenReturn(prUser);
        when(pr.getId()).thenReturn(prId);
        PullRequestRef toRef = mock(PullRequestRef.class);
        when(pr.getToRef()).thenReturn(toRef);
        when(toRef.getDisplayId()).thenReturn(prDest);
        when(prUser.getDisplayName()).thenReturn(authorName);
        when(prUser.getEmailAddress()).thenReturn(authorEmail);
        when(pr.getTitle()).thenReturn(title);
        when(pr.getDescription()).thenReturn(description);
        when(prService.getById(repository.getId(), prId)).thenReturn(pr);
        Response actual = rest.getJobs(repository, "branch", "commit", prDest, prId);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> jobData = (List<Map<String, Object>>) actual.getEntity();
        List<Map<String, Object>> parameters = new ArrayList<>();
        Map<String, Object> parameter1 = new HashMap<>();
        parameter1.put("param1", "branch");
        Map<String, Object> parameter2 = new HashMap<>();
        parameter2.put("param2", prDest);
        Map<String, Object> parameter3 = new HashMap<>();
        parameter3.put("param3", URI + "/projects/" + PROJECT_KEY + "/repos/" + REPO_SLUG +
                "/pull-requests/" + prId);
        Map<String, Object> parameter4 = new HashMap<>();
        parameter4.put("param4", authorName);
        Map<String, Object> parameter5 = new HashMap<>();
        parameter5.put("param5", authorEmail);
        Map<String, Object> parameter6 = new HashMap<>();
        parameter6.put("param6", title);
        Map<String, Object> parameter7 = new HashMap<>();
        parameter7.put("param7", description);
        Map<String, Object> parameter8 = new HashMap<>();
        parameter8.put("param8", Long.toString(prId));
        parameters.add(parameter1);
        parameters.add(parameter2);
        parameters.add(parameter3);
        parameters.add(parameter4);
        parameters.add(parameter5);
        parameters.add(parameter6);
        parameters.add(parameter7);
        parameters.add(parameter8);
        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
        assertEquals(jobId, jobData.get(0).get("id"));
        assertEquals(jobName, jobData.get(0).get("jobName"));
        assertEquals(parameters, jobData.get(0).get("buildParameters"));
    }

    @Test
    public void testGetJobsWithNoPR() {
        String jobName = "jobname";
        Job job = new Job.JobBuilder(1).jobName(jobName).triggers(new String[] { "manual" })
                .buildParameters("param2=$PRDESTINATION").permissions("REPO_ADMIN").build();
        jobs.add(job);
        Response actual = rest.getJobs(repository, "branch", "commit", null, 0);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> jobData = (List<Map<String, Object>>) actual.getEntity();
        List<Map<String, Object>> parameters = new ArrayList<>();
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("param2", "$PRDESTINATION");
        parameters.add(parameter);
        assertEquals(parameters, jobData.get(0).get("buildParameters"));
    }


    @Test
    public void testGetHookEnabled() {
        when(hook.isEnabled()).thenReturn(true);
        Response actual = rest.getHookEnabled(repository);

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
        assert (boolean) actual.getEntity();
    }

    @Test
    public void testGetHookNotEnabled() {
        when(hook.isEnabled()).thenReturn(false);
        Response actual = rest.getHookEnabled(repository);

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
        assert  !(boolean) actual.getEntity();
    }

    @Test
    public void testGetHookEnabledNull() {
        when(settingsService.getHook(any())).thenReturn(null);
        Response actual = rest.getHookEnabled(repository);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testGetHookEnabledNotAuthed() {
        when(authContext.isAuthenticated()).thenReturn(false);
        Response actual = rest.getHookEnabled(repository);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), actual.getStatus());
    }
}
