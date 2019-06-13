package com.kylenicholls.stash.parameterizedbuilds.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.MultivaluedMap;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import static org.mockito.Mockito.when;

public class ProjectResourceTest {
    private ProjectResource rest;
    private Jenkins jenkins;
    private AuthenticationContext authContext;
    private ApplicationUser user;
    private UriInfo ui;
    private Server projectServer;
    private String projectKey;

    @Before
    public void setup() throws Exception {
        projectServer = new Server("projecturl", "project server", "projectuser", "projecttoken", false, false);
        projectKey = "TEST";
        I18nService i18nService = mock(I18nService.class);
        jenkins = mock(Jenkins.class);
        authContext = mock(AuthenticationContext.class);
        rest = new ProjectResource(i18nService, jenkins, authContext);
        ui = mock(UriInfo.class);
        MultivaluedMap<String, String> paramMap = mock(MultivaluedMap.class);

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getCurrentUser()).thenReturn(user);
        when(ui.getPathParameters()).thenReturn(paramMap);
        when(paramMap.getFirst("projectKey")).thenReturn(projectKey);
    }
    
    @Test
    public void testGetServersEmpty(){
        when(jenkins.getJenkinsServer(projectKey)).thenReturn(null);
        Response actual = rest.getServers(ui);

        assertEquals(Lists.newArrayList(), actual.getEntity());
    }

    @Test
    public void testGetServersOkStatus(){
        when(jenkins.getJenkinsServer(projectKey)).thenReturn(null);
        Response actual = rest.getServers(ui);

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testGetServersSet(){
        when(jenkins.getJenkinsServer(projectKey)).thenReturn(projectServer);
        Response actual = rest.getServers(ui);

        Map<String, Object> expected = rest.createServerMap(projectServer, projectKey);

        assertEquals(Lists.newArrayList(expected), actual.getEntity());
    }

    @Test
    public void testValidateServerReturnsSuccessMessage(){
        String expected = "Connection successful";
        when(jenkins.testConnection(projectServer)).thenReturn(expected);
        Response actual = rest.validate(ui, projectServer);

        assertEquals(expected, actual.getEntity());
    }

    @Test
    public void testValidateServerReturnsOkStatus(){
        String expected = "Connection successful";
        when(jenkins.testConnection(projectServer)).thenReturn(expected);
        Response actual = rest.validate(ui, projectServer);

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testValidateServerReturnsFailureMessage(){
        String expected = "Failed to establish connection";
        when(jenkins.testConnection(projectServer)).thenReturn(expected);
        Response actual = rest.validate(ui, projectServer);

        assertEquals(expected, actual.getEntity());
    }

    @Test
    public void testValidateServerReturnsFailureStatus(){
        String expected = "Failed to establish connection";
        when(jenkins.testConnection(projectServer)).thenReturn(expected);
        Response actual = rest.validate(ui, projectServer);

        assertEquals(400, actual.getStatus());
    }

    @Test
    public void testAddServerReturns200OnUpdate(){
        when(jenkins.getJenkinsServer(projectKey)).thenReturn(projectServer);
        Response actual = rest.addServer(ui, projectServer);

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testAddServerReturns201OnCreate(){
        when(jenkins.getJenkinsServer(projectKey)).thenReturn(null);
        Response actual = rest.addServer(ui, projectServer);

        assertEquals(Response.Status.CREATED.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testAddServerPreservesToken(){
        when(jenkins.getJenkinsServer(projectKey)).thenReturn(projectServer);
        Server testServer = rest.mapToServer(projectServer.asMap());
        testServer.setToken(null);
        rest.addServer(ui, testServer);

        assertEquals(projectServer.getToken(), testServer.getToken());
    }

    @Test
    public void testAddServerRemovesEmptyStringToken(){
        when(jenkins.getJenkinsServer(projectKey)).thenReturn(projectServer);
        Server testServer = rest.mapToServer(projectServer.asMap());
        testServer.setToken("");
        rest.addServer(ui, testServer);

        assertEquals("", testServer.getToken());
    }

    @Test
    public void testAddServerRemovesTokenIfDifferentURL(){
        when(jenkins.getJenkinsServer(projectKey)).thenReturn(projectServer);
        Server testServer = rest.mapToServer(projectServer.asMap());
        testServer.setToken(null);
        testServer.setBaseUrl("different");
        rest.addServer(ui, testServer);

        assertEquals("", testServer.getToken());
    }

    @Test
    public void testAddServerRemovesTokenIfDifferentUser(){
        when(jenkins.getJenkinsServer(projectKey)).thenReturn(projectServer);
        Server testServer = rest.mapToServer(projectServer.asMap());
        testServer.setToken(null);
        testServer.setUser("different");
        rest.addServer(ui, testServer);

        assertEquals("", testServer.getToken());
    }

    @Test
    public void testAddServerReturns422OnMissingAlias(){
        projectServer.setAlias("");
        when(jenkins.getJenkinsServer(projectKey)).thenReturn(null);
        Response actual = rest.addServer(ui, projectServer);

        assertEquals(422, actual.getStatus());
    }

    @Test
    public void testAddServerReturns422OnMissingUrl(){
        projectServer.setBaseUrl("");
        when(jenkins.getJenkinsServer(projectKey)).thenReturn(null);
        Response actual = rest.addServer(ui, projectServer);

        assertEquals(422, actual.getStatus());
    }

    @Test
    public void testAddServerReturnsErrorMessageOnMissingUrl(){
        projectServer.setBaseUrl("");
        when(jenkins.getJenkinsServer(projectKey)).thenReturn(null);
        Response actual = rest.addServer(ui, projectServer);

        String response = actual.getEntity().toString();
        List<String> errors = (List<String>) new Gson().fromJson(response, Map.class).get("errors");

        assertEquals(Lists.newArrayList("Base Url required."), errors);
    }

    @Test
    public void testAddServerReturnsAllErrorMessages(){
        projectServer.setBaseUrl("");
        projectServer.setAlias("");
        when(jenkins.getJenkinsServer(projectKey)).thenReturn(null);
        Response actual = rest.addServer(ui, projectServer);

        String response = actual.getEntity().toString();
        List<String> errors = (List<String>) new Gson().fromJson(response, Map.class).get("errors");

        assertEquals(Lists.newArrayList("Base Url required.", "Alias required."), errors);
    }

    @Test
    public void testRemoveServerRemovesServer(){
        rest.removeServer(ui);
        verify(jenkins, times(1)).saveJenkinsServer(null, projectKey);
    }

    @Test
    public void testRemoveServerReturnsNoContent(){
        Response actual = rest.removeServer(ui);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), actual.getStatus());
    }
}