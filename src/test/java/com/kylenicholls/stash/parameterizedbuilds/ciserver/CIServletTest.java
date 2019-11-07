package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.webresource.api.assembler.RequiredResources;
import com.atlassian.webresource.api.assembler.WebResourceAssembler;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import com.kylenicholls.stash.parameterizedbuilds.item.UserToken;

public class CIServletTest {
    private final String SOY_TEMPLATE = 
            "com.kylenicholls.stash.parameterized-builds:jenkins-admin-soy";
    private final String PROJECT_KEY = "projkey";
    private final String USER_SLUG = "userslug";
    private final String GLOBAL_PATH = "/plugins/servlet/jenkins";
    private final String PROJECT_PATH = "/plugins/servlet/jenkins/project/";
    private final String ACCOUNT_PATH = "/plugins/servlet/jenkins/account";
    private final String CONTEXT_KEY = "bitbucketContext";
    private final String BITBUCKET_CONTEXT = "/bitbucket";
    private CIServlet servlet;
    private SoyTemplateRenderer renderer;
    private Jenkins jenkins;
    private AuthenticationContext authContext;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private ApplicationUser user;
    private Project project;
    private ProjectService projectService;
    private PageBuilderService pageBuilderService;

    @Before
    public void setup() throws IOException {
        renderer = mock(SoyTemplateRenderer.class);
        authContext = mock(AuthenticationContext.class);
        NavBuilder navBuilder = mock(NavBuilder.class);
        jenkins = mock(Jenkins.class);
        projectService = mock(ProjectService.class);
        pageBuilderService = mock(PageBuilderService.class);
        servlet = new CIServlet(renderer, authContext, navBuilder, jenkins, projectService, 
                pageBuilderService);

        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        user = mock(ApplicationUser.class);
        project = mock(Project.class);

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getCurrentUser()).thenReturn(user);
        when(user.getSlug()).thenReturn(USER_SLUG);
        when(project.getKey()).thenReturn(PROJECT_KEY);
        when(projectService.getByKey(PROJECT_KEY)).thenReturn(project);

        WebResourceAssembler mockAssembler = mock(WebResourceAssembler.class);
        RequiredResources mockResources = mock(RequiredResources.class);

        when(pageBuilderService.assembler()).thenReturn(mockAssembler);
        when(mockAssembler.resources()).thenReturn(mockResources);
        when(mockResources.requireWebResource(any())).thenReturn(null);
        when(navBuilder.buildRelative()).thenReturn(BITBUCKET_CONTEXT);
    }

    @Test
    public void testDoGetGlobalServer() throws ServletException, IOException, SoyException {
        when(req.getPathInfo()).thenReturn(GLOBAL_PATH);
        Server server = new Server("baseurl", null, null, null, false, false);
        List<Server> servers = Lists.newArrayList(server);
        when(jenkins.getJenkinsServers(null)).thenReturn(servers);
        servlet.doGet(req, resp);

        Map<String, Object> data = ImmutableMap.of(
                CIServer.SERVER, servers,
                CONTEXT_KEY, BITBUCKET_CONTEXT);
        verify(renderer, times(1))
                .render(resp.getWriter(), SOY_TEMPLATE, "jenkins.admin.settings", data);
    }

    @Test
    public void testDoGetGlobalServerNull() throws ServletException, IOException, SoyException {
        when(req.getPathInfo()).thenReturn(GLOBAL_PATH);
        when(jenkins.getJenkinsServers(null)).thenReturn(Lists.newArrayList());
        servlet.doGet(req, resp);

        Map<String, Object> data = ImmutableMap.of(
                CIServer.SERVER, Lists.newArrayList(),
                CONTEXT_KEY, BITBUCKET_CONTEXT);
        verify(renderer, times(1))
                .render(resp.getWriter(), SOY_TEMPLATE, "jenkins.admin.settings", data);
    }

    @Test
    public void testDoGetAccountServerNull() throws ServletException, IOException, SoyException {
        List<UserToken> projectTokens = new ArrayList<>();
        when(req.getPathInfo()).thenReturn(ACCOUNT_PATH);
        when(jenkins.getJenkinsServers(null)).thenReturn(Lists.newArrayList());
        when(jenkins.getAllUserTokens(user, new ArrayList<String>(), projectService))
                .thenReturn(projectTokens);
        servlet.doGet(req, resp);

        Map<String, Object> data = ImmutableMap
                .<String, Object> of(
                    "user", user,
                    "projectTokens", "[]",
                    CONTEXT_KEY, BITBUCKET_CONTEXT);
        verify(renderer, times(1))
                .render(resp.getWriter(), SOY_TEMPLATE, "jenkins.user.settings", data);
    }

    @Test
    public void testDoGetProjectServer() throws ServletException, IOException, SoyException {
        when(req.getPathInfo()).thenReturn(PROJECT_PATH + PROJECT_KEY);
        Server server = new Server("baseurl", null, null, null, false, false);
        List<Server> servers = Lists.newArrayList(server);
        when(jenkins.getJenkinsServers(PROJECT_KEY)).thenReturn(servers);
        servlet.doGet(req, resp);

        Map<String, Object> data = ImmutableMap.of(
                CIServer.SERVER, servers,
                ProjectServer.PROJECT_KEY, PROJECT_KEY,
                CONTEXT_KEY, BITBUCKET_CONTEXT);
        verify(renderer, times(1))
                .render(resp.getWriter(), SOY_TEMPLATE, "jenkins.admin.settingsProjectAdmin", data);
    }

    @Test
    public void testDoGetProjectServerNull() throws ServletException, IOException, SoyException {
        when(req.getPathInfo()).thenReturn(PROJECT_PATH + PROJECT_KEY);
        when(jenkins.getJenkinsServers(PROJECT_KEY)).thenReturn(Lists.newArrayList());
        servlet.doGet(req, resp);

        Map<String, Object> data = ImmutableMap.of(
                CIServer.SERVER, Lists.newArrayList(),
                ProjectServer.PROJECT_KEY, PROJECT_KEY,
                CONTEXT_KEY, BITBUCKET_CONTEXT);
        verify(renderer, times(1))
                .render(resp.getWriter(), SOY_TEMPLATE, "jenkins.admin.settingsProjectAdmin", data);
    }
}
