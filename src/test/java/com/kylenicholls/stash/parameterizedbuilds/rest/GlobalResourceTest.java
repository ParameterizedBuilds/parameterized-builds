package com.kylenicholls.stash.parameterizedbuilds.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.JenkinsConnection;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GlobalResource.class, JenkinsConnection.class})
@PowerMockIgnore("javax.security.*")
public class GlobalResourceTest {
    private GlobalResource rest;
    private Jenkins jenkins;
    private JenkinsConnection jenkinsConn;
    private AuthenticationContext authContext;
    private ApplicationUser user;
    private UriInfo ui;
    private Server globalServer;
    private List<Server> globalServers;
    private ServerService.Token testToken;

    private final String TOKEN_VALUE = "myToken";
    private final String USER_SLUG = "myUser";

    @Before
    public void setup() throws Exception {
<<<<<<< HEAD
        globalServer = new Server("http://globalurl", "global server", "globaluser", "globaltoken",
                false, false);
=======
        globalServer = new Server("http://globalurl", "global server", "globaluser", "globaltoken", false, false);
        globalServers = Lists.newArrayList(globalServer);
>>>>>>> Update get server api
        I18nService i18nService = mock(I18nService.class);
        jenkins = mock(Jenkins.class);
        authContext = mock(AuthenticationContext.class);
        rest = new GlobalResource(i18nService, jenkins, authContext);
        ui = mock(UriInfo.class);
        testToken = new ServerService.Token();
        testToken.setToken(TOKEN_VALUE);
        user = mock(ApplicationUser.class);

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getCurrentUser()).thenReturn(user);
        when(user.getSlug()).thenReturn(USER_SLUG);

        jenkinsConn = mock(JenkinsConnection.class);
        PowerMockito.whenNew(JenkinsConnection.class)
            .withArguments(jenkins)
            .thenReturn(jenkinsConn);
    }
    
    @Test
    public void testGetServersEmpty(){
        when(jenkins.getJenkinsServers(null)).thenReturn(Lists.newArrayList());
        Response actual = rest.getServers(ui);

        assertEquals(Lists.newArrayList(), actual.getEntity());
    }

    @Test
    public void testGetServersOkStatus(){
        when(jenkins.getJenkinsServers(null)).thenReturn(Lists.newArrayList());
        Response actual = rest.getServers(ui);

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testGetServersSet(){
        when(jenkins.getJenkinsServers(null)).thenReturn(globalServers);
        Response actual = rest.getServers(ui);

        Map<String, Object> expected = rest.createServerMap(globalServer, null);

        assertEquals(Lists.newArrayList(expected), actual.getEntity());
    }

    @Test
    public void testValidateServerReturnsSuccessMessage(){
        String expected = "Connection successful";
        when(jenkinsConn.testConnection(globalServer)).thenReturn(expected);
        Response actual = rest.validate(ui, globalServer);

        assertEquals(expected, actual.getEntity());
    }

    @Test
    public void testValidateServerReturnsOkStatus(){
        String expected = "Connection successful";
        when(jenkinsConn.testConnection(globalServer)).thenReturn(expected);
        Response actual = rest.validate(ui, globalServer);

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testValidateServerReturnsFailureMessage(){
        String expected = "Failed to establish connection";
        when(jenkinsConn.testConnection(globalServer)).thenReturn(expected);
        Response actual = rest.validate(ui, globalServer);

        assertEquals(expected, actual.getEntity());
    }

    @Test
    public void testValidateServerReturnsFailureStatus(){
        String expected = "Failed to establish connection";
        when(jenkinsConn.testConnection(globalServer)).thenReturn(expected);
        Response actual = rest.validate(ui, globalServer);

        assertEquals(400, actual.getStatus());
    }

    @Test
    public void testValidateServerPreservesToken(){
        when(jenkins.getJenkinsServer(isNull(), any())).thenReturn(globalServer);
        Server testServer = rest.mapToServer(globalServer.asMap());
        when(jenkinsConn.testConnection(testServer)).thenReturn( "Connection successful");
        testServer.setToken(null);
        rest.validate(ui, testServer);

        assertEquals(globalServer.getToken(), testServer.getToken());
        assertNotNull(testServer.getToken());
    }

    @Test
    public void testAddServerReturns200OnUpdate(){
        when(jenkins.getJenkinsServer(isNull(), any())).thenReturn(globalServer);
        Response actual = rest.addServer(ui, globalServer, globalServer.getAlias());

        assertEquals(Response.Status.OK.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testAddServerReturns201OnCreate(){
        when(jenkins.getJenkinsServer(isNull(), any())).thenReturn(null);
        Response actual = rest.addServer(ui, globalServer, globalServer.getAlias());

        assertEquals(Response.Status.CREATED.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testAddServerPreservesToken(){
        when(jenkins.getJenkinsServer(isNull(), any())).thenReturn(globalServer);
        Server testServer = rest.mapToServer(globalServer.asMap());
        testServer.setToken(null);
        rest.addServer(ui, testServer, testServer.getAlias());

        assertEquals(globalServer.getToken(), testServer.getToken());
    }

    @Test
    public void testAddServerRemovesEmptyStringToken(){
        when(jenkins.getJenkinsServer(isNull(), any())).thenReturn(globalServer);
        Server testServer = rest.mapToServer(globalServer.asMap());
        testServer.setToken("");
        rest.addServer(ui, testServer, testServer.getAlias());

        assertEquals("", testServer.getToken());
    }

    @Test
    public void testAddServerRemovesTokenIfDifferentURL(){
        when(jenkins.getJenkinsServer(isNull(), any())).thenReturn(globalServer);
        Server testServer = rest.mapToServer(globalServer.asMap());
        testServer.setToken(null);
        testServer.setBaseUrl("http://different");
        rest.addServer(ui, testServer, testServer.getAlias());

        assertEquals("", testServer.getToken());
    }

    @Test
    public void testAddServerRemovesTokenIfDifferentUser(){
        when(jenkins.getJenkinsServer(isNull(), any())).thenReturn(globalServer);
        Server testServer = rest.mapToServer(globalServer.asMap());
        testServer.setToken(null);
        testServer.setUser("different");
        rest.addServer(ui, testServer, testServer.getAlias());

        assertEquals("", testServer.getToken());
    }

    @Test
    public void testAddServerReturns422OnMissingAlias(){
        globalServer.setAlias("");
        when(jenkins.getJenkinsServer(isNull(), any())).thenReturn(null);
        Response actual = rest.addServer(ui, globalServer, globalServer.getAlias());

        assertEquals(422, actual.getStatus());
    }

    @Test
    public void testAddServerReturns422OnMissingUrl(){
        globalServer.setBaseUrl("");
        when(jenkins.getJenkinsServer(isNull(), any())).thenReturn(null);
        Response actual = rest.addServer(ui, globalServer, globalServer.getAlias());

        assertEquals(422, actual.getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddServerReturnsErrorMessageOnMissingUrl(){
        globalServer.setBaseUrl("");
        when(jenkins.getJenkinsServer(isNull(), any())).thenReturn(null);
        Response actual = rest.addServer(ui, globalServer, globalServer.getAlias());

        String response = actual.getEntity().toString();
        List<String> errors = (List<String>) new Gson().fromJson(response, Map.class).get("errors");

        assertEquals(Lists.newArrayList("Base Url required."), errors);
    }

    @Test
    public void testAddServerReturns422OnBadUrl(){
        globalServer.setBaseUrl("noprotocal");
        when(jenkins.getJenkinsServer(isNull(), any())).thenReturn(null);
        Response actual = rest.addServer(ui, globalServer, globalServer.getAlias());

        assertEquals(422, actual.getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddServerReturnsErrorMessageOnBadUrl(){
        globalServer.setBaseUrl("noprotocal");
        when(jenkins.getJenkinsServer(isNull(), any())).thenReturn(null);
        Response actual = rest.addServer(ui, globalServer, globalServer.getAlias());

        String response = actual.getEntity().toString();
        List<String> errors = (List<String>) new Gson().fromJson(response, Map.class).get("errors");

        assertEquals(Lists.newArrayList("Invalide Base Url."), errors);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddServerReturnsAllErrorMessages(){
        globalServer.setBaseUrl("");
        globalServer.setAlias("");
        when(jenkins.getJenkinsServer(isNull(), any())).thenReturn(null);
        Response actual = rest.addServer(ui, globalServer, globalServer.getAlias());

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

    @Test
    public void testAddTokenAddsToken(){
        rest.addUserToken(ui, testToken);
        verify(jenkins, times(1)).saveUserToken(USER_SLUG, "", TOKEN_VALUE);
    }

    @Test
    public void testAddTokenAddsEmptyToken(){
        ServerService.Token emptyToken = new ServerService.Token();
        emptyToken.setToken("");
        rest.addUserToken(ui, emptyToken);
        verify(jenkins, times(1)).saveUserToken(USER_SLUG, "", "");
    }

    @Test
    public void testAddTokenReturnsNonContent(){
        Response actual = rest.addUserToken(ui, testToken);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), actual.getStatus());
    }

    @Test
    public void testRemoveTokenRemovesToken(){
        rest.removeUserToken(ui);
        verify(jenkins, times(1)).saveUserToken(USER_SLUG, "", "");
    }

    @Test
    public void testAddRemoveReturnsNonContent(){
        Response actual = rest.removeUserToken(ui);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), actual.getStatus());
    }
}