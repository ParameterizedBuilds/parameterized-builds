package com.kylenicholls.stash.parameterizedbuilds.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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

public class GlobalResourceTest {
    private GlobalResource rest;
    private Jenkins jenkins;
    private AuthenticationContext authContext;
    private ApplicationUser user;
    private UriInfo ui;
    private Server globalServer;

    @Before
    public void setup() throws Exception {
        globalServer = new Server("globalurl", "global server", "globaluser", "globaltoken", false, false);
        I18nService i18nService = mock(I18nService.class);
        jenkins = mock(Jenkins.class);
        authContext = mock(AuthenticationContext.class);
        rest = new GlobalResource(i18nService, jenkins, authContext);
        ui = mock(UriInfo.class);

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getCurrentUser()).thenReturn(user);
    }
    
    @Test
    public void testGetServersEmpty(){
        when(jenkins.getJenkinsServer(null)).thenReturn(null);
        Response actual = rest.getServers(ui);

        assertEquals(Lists.newArrayList(), actual.getEntity());
    }

    @Test
    public void testGetServersOkStatus(){
        when(jenkins.getJenkinsServer(null)).thenReturn(null);
        Response actual = rest.getServers(ui);

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testGetServersSet(){
        when(jenkins.getJenkinsServer(null)).thenReturn(globalServer);
        Response actual = rest.getServers(ui);

        Map<String, Object> expected = rest.createServerMap(globalServer, null);

        assertEquals(Lists.newArrayList(expected), actual.getEntity());
    }

    @Test
    public void testValidateServerReturnsSuccessMessage(){
        String expected = "Connection successful";
        when(jenkins.testConnection(globalServer)).thenReturn(expected);
        Response actual = rest.validate(ui, globalServer);

        assertEquals(expected, actual.getEntity());
    }

    @Test
    public void testValidateServerReturnsOkStatus(){
        String expected = "Connection successful";
        when(jenkins.testConnection(globalServer)).thenReturn(expected);
        Response actual = rest.validate(ui, globalServer);

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testValidateServerReturnsFailureMessage(){
        String expected = "Failed to establish connection";
        when(jenkins.testConnection(globalServer)).thenReturn(expected);
        Response actual = rest.validate(ui, globalServer);

        assertEquals(expected, actual.getEntity());
    }

    @Test
    public void testValidateServerReturnsFailureStatus(){
        String expected = "Failed to establish connection";
        when(jenkins.testConnection(globalServer)).thenReturn(expected);
        Response actual = rest.validate(ui, globalServer);

        assertEquals(400, actual.getStatus());
    }

    @Test
    public void testAddServerReturns200OnUpdate(){
        when(jenkins.getJenkinsServer(null)).thenReturn(globalServer);
        Response actual = rest.addServer(ui, globalServer);

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testAddServerReturns201OnCreate(){
        when(jenkins.getJenkinsServer(null)).thenReturn(null);
        Response actual = rest.addServer(ui, globalServer);

        assertEquals(Response.Status.CREATED.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testAddServerPreservesToken(){
        when(jenkins.getJenkinsServer(null)).thenReturn(globalServer);
        Server testServer = rest.mapToServer(globalServer.asMap());
        testServer.setToken(null);
        rest.addServer(ui, testServer);

        assertEquals(globalServer.getToken(), testServer.getToken());
    }

    @Test
    public void testAddServerRemovesEmptyStringToken(){
        when(jenkins.getJenkinsServer(null)).thenReturn(globalServer);
        Server testServer = rest.mapToServer(globalServer.asMap());
        testServer.setToken("");
        rest.addServer(ui, testServer);

        assertEquals("", testServer.getToken());
    }

    @Test
    public void testAddServerRemovesTokenIfDifferentURL(){
        when(jenkins.getJenkinsServer(null)).thenReturn(globalServer);
        Server testServer = rest.mapToServer(globalServer.asMap());
        testServer.setToken(null);
        testServer.setBaseUrl("different");
        rest.addServer(ui, testServer);

        assertEquals("", testServer.getToken());
    }

    @Test
    public void testAddServerRemovesTokenIfDifferentUser(){
        when(jenkins.getJenkinsServer(null)).thenReturn(globalServer);
        Server testServer = rest.mapToServer(globalServer.asMap());
        testServer.setToken(null);
        testServer.setUser("different");
        rest.addServer(ui, testServer);

        assertEquals("", testServer.getToken());
    }

    @Test
    public void testAddServerReturns422OnMissingAlias(){
        globalServer.setAlias("");
        when(jenkins.getJenkinsServer(null)).thenReturn(null);
        Response actual = rest.addServer(ui, globalServer);

        assertEquals(422, actual.getStatus());
    }

    @Test
    public void testAddServerReturns422OnMissingUrl(){
        globalServer.setBaseUrl("");
        when(jenkins.getJenkinsServer(null)).thenReturn(null);
        Response actual = rest.addServer(ui, globalServer);

        assertEquals(422, actual.getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddServerReturnsErrorMessageOnMissingUrl(){
        globalServer.setBaseUrl("");
        when(jenkins.getJenkinsServer(null)).thenReturn(null);
        Response actual = rest.addServer(ui, globalServer);

        String response = actual.getEntity().toString();
        List<String> errors = (List<String>) new Gson().fromJson(response, Map.class).get("errors");

        assertEquals(Lists.newArrayList("Base Url required."), errors);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddServerReturnsAllErrorMessages(){
        globalServer.setBaseUrl("");
        globalServer.setAlias("");
        when(jenkins.getJenkinsServer(null)).thenReturn(null);
        Response actual = rest.addServer(ui, globalServer);

        String response = actual.getEntity().toString();
        List<String> errors = (List<String>) new Gson().fromJson(response, Map.class).get("errors");

        assertEquals(Lists.newArrayList("Base Url required.", "Alias required."), errors);
    }

    @Test
    public void testRemoveServerRemovesServer(){
        rest.removeServer(ui);
        verify(jenkins, times(1)).saveJenkinsServer(null, null);
    }

    @Test
    public void testRemoveServerReturnsNoContent(){
        Response actual = rest.removeServer(ui);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), actual.getStatus());
    }
}