package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.google.common.collect.ImmutableMap;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import com.kylenicholls.stash.parameterizedbuilds.item.UserToken;

public class CIServletTest {
	private final String USER_TOKEN_PREFIX = "jenkinsToken-";
	private final String PROJECT_KEY_PREFIX = "projectKey-";
	private final String SOY_TEMPLATE = "com.kylenicholls.stash.parameterized-builds:jenkins-admin-soy";
	private final String PROJECT_KEY = "projkey";
	private final String USER_SLUG = "userslug";
	private final String GLOBAL_PATH = "/plugins/servlet/jenkins";
	private final String PROJECT_PATH = "/plugins/servlet/jenkins/project/";
	private final String ACCOUNT_PATH = "/plugins/servlet/jenkins/account";
	private CIServlet servlet;
	private SoyTemplateRenderer renderer;
	private Jenkins jenkins;
	private AuthenticationContext authContext;
	private HttpServletRequest req;
	private HttpServletResponse resp;
	private ApplicationUser user;
	private Project project;
	private ProjectService projectService;

	@Before
	public void setup() throws IOException {
		renderer = mock(SoyTemplateRenderer.class);
		authContext = mock(AuthenticationContext.class);
		NavBuilder navBuilder = mock(NavBuilder.class);
		jenkins = mock(Jenkins.class);
		projectService = mock(ProjectService.class);
		servlet = new CIServlet(renderer, authContext, navBuilder, jenkins, projectService);

		req = mock(HttpServletRequest.class);
		resp = mock(HttpServletResponse.class);
		user = mock(ApplicationUser.class);
		project = mock(Project.class);

		when(authContext.isAuthenticated()).thenReturn(true);
		when(authContext.getCurrentUser()).thenReturn(user);
		when(user.getSlug()).thenReturn(USER_SLUG);
		when(project.getKey()).thenReturn(PROJECT_KEY);
		when(projectService.getByKey(PROJECT_KEY)).thenReturn(project);
	}

	@Test
	public void testDoGetGlobalServer() throws ServletException, IOException, SoyException {
		when(req.getPathInfo()).thenReturn(GLOBAL_PATH);
		Server server = new Server("baseurl", null, null, false, false);
		when(jenkins.getJenkinsServer()).thenReturn(server);
		servlet.doGet(req, resp);

		Map<String, Object> data = ImmutableMap.of(
				CIServer.SERVER, server,
				CIServer.ERRORS, "",
				CIServer.TESTMESSAGE, "");
		verify(renderer, times(1))
				.render(resp.getWriter(), SOY_TEMPLATE, "jenkins.admin.settings", data);
	}

	@Test
	public void testDoGetGlobalServerNull() throws ServletException, IOException, SoyException {
		when(req.getPathInfo()).thenReturn(GLOBAL_PATH);
		when(jenkins.getJenkinsServer()).thenReturn(null);
		servlet.doGet(req, resp);

		Map<String, Object> data = ImmutableMap.of(
				CIServer.SERVER, "",
				CIServer.ERRORS, "",
				CIServer.TESTMESSAGE, "");
		verify(renderer, times(1))
				.render(resp.getWriter(), SOY_TEMPLATE, "jenkins.admin.settings", data);
	}

	@Test
	public void testDoGetAccountServerNull() throws ServletException, IOException, SoyException {
		List<UserToken> projectTokens = new ArrayList<>();
		when(req.getPathInfo()).thenReturn(ACCOUNT_PATH);
		when(jenkins.getJenkinsServer()).thenReturn(null);
		when(jenkins.getAllUserTokens(user, new ArrayList<String>(), projectService))
				.thenReturn(projectTokens);
		servlet.doGet(req, resp);

		Map<String, Object> data = ImmutableMap
				.<String, Object> of("user", user, "projectTokens", projectTokens, "errors", "");
		verify(renderer, times(1))
				.render(resp.getWriter(), SOY_TEMPLATE, "jenkins.user.settings", data);
	}

	@Test
	public void testDoGetProjectServer() throws ServletException, IOException, SoyException {
		when(req.getPathInfo()).thenReturn(PROJECT_PATH + PROJECT_KEY);
		Server server = new Server("baseurl", null, null, false, false);
		when(jenkins.getJenkinsServer(PROJECT_KEY)).thenReturn(server);
		servlet.doGet(req, resp);

		Map<String, Object> data = ImmutableMap.of(
				CIServer.SERVER, server,
				ProjectServer.PROJECT_KEY, PROJECT_KEY,
				CIServer.ERRORS, "",
				CIServer.TESTMESSAGE, "");
		verify(renderer, times(1))
				.render(resp.getWriter(), SOY_TEMPLATE, "jenkins.admin.settingsProjectAdmin", data);
	}

	@Test
	public void testDoGetProjectServerNull() throws ServletException, IOException, SoyException {
		when(req.getPathInfo()).thenReturn(PROJECT_PATH + PROJECT_KEY);
		when(jenkins.getJenkinsServer(PROJECT_KEY)).thenReturn(null);
		servlet.doGet(req, resp);

		Map<String, Object> data = ImmutableMap.of(
				CIServer.SERVER, "",
				ProjectServer.PROJECT_KEY, PROJECT_KEY,
				CIServer.ERRORS, "",
				CIServer.TESTMESSAGE, "");
		verify(renderer, times(1))
				.render(resp.getWriter(), SOY_TEMPLATE, "jenkins.admin.settingsProjectAdmin", data);
	}

	@Test
	public void testDoPostAccountSettings() throws ServletException, IOException, SoyException {
		String globalToken = "globaltoken";
		String projectToken = "projecttoken";
		Map<String, String[]> parameterMap = new HashMap<>();
		parameterMap.put(PROJECT_KEY_PREFIX, new String[] { "" });
		parameterMap.put(USER_TOKEN_PREFIX, new String[] { globalToken });
		parameterMap.put(PROJECT_KEY_PREFIX + PROJECT_KEY, new String[] { PROJECT_KEY });
		parameterMap.put(USER_TOKEN_PREFIX + PROJECT_KEY, new String[] { projectToken });
		String saveButton = "'Save Connection";


		when(req.getPathInfo()).thenReturn(ACCOUNT_PATH);
		when(req.getParameterMap()).thenReturn(parameterMap);
		when(req.getParameter("jenkinsToken")).thenReturn("token");
		when(req.getParameter("submit")).thenReturn(saveButton);
		servlet.doPost(req, resp);

		verify(jenkins, times(1)).saveUserToken(USER_SLUG, "", globalToken);
		verify(jenkins, times(1)).saveUserToken(USER_SLUG, PROJECT_KEY, projectToken);
		verify(renderer, times(1)).render(any(), any(), any(), any());
	}

	@Test
	public void testDoPostGlobalSettings() throws ServletException, IOException, SoyException {
		String[] baseUrl = {"baseurl"};
		String[] defaultUser = {"defaultuser"};
		String[] defaultToken = {"defaulttoken"};
		String[] altUrl = {"on"};
		String[] csrfEnabled = {"on"};
		String saveButton = "'Save Connection";
		Map<String, String[]> params = ImmutableMap.<String, String[]>builder()
				.put("jenkinsUrl", baseUrl)
				.put("jenkinsUser", defaultUser)
				.put("jenkinsToken",  defaultToken)
				.put("jenkinsAltUrl", altUrl)
				.put("jenkinsCSRF", csrfEnabled)
				.build();

		when(req.getParameterMap()).thenReturn(params);
		when(req.getPathInfo()).thenReturn(GLOBAL_PATH);
		when(req.getParameter("submit")).thenReturn(saveButton);
		servlet.doPost(req, resp);

		verify(jenkins, times(1)).saveJenkinsServer(isNotNull(Server.class));
		verify(renderer, times(1)).render(any(), any(), any(), any());
	}

	@Test
	public void testDoPostGlobalSettingsEmptyUrl()
			throws ServletException, IOException, SoyException {
		String[] baseUrl = {""};
		String[] defaultUser = {"defaultuser"};
		String[] defaultToken = {"defaulttoken"};
		String[] altUrl = {"on"};
		String[] csrfEnabled = {"on"};
		String saveButton = "'Save Connection";
		Map<String, String[]> params = ImmutableMap.<String, String[]>builder()
				.put("jenkinsUrl", baseUrl)
				.put("jenkinsUser", defaultUser)
				.put("jenkinsToken",  defaultToken)
				.put("jenkinsAltUrl", altUrl)
				.put("jenkinsCSRF", csrfEnabled)
				.build();

		when(req.getParameterMap()).thenReturn(params);
		when(req.getPathInfo()).thenReturn(GLOBAL_PATH);
		when(req.getParameter("submit")).thenReturn(saveButton);
		servlet.doPost(req, resp);

		verify(renderer, times(1)).render(any(), any(), any(), any());
	}

	// @Test
	public void testDoPostGlobalSettingsAltUrlNull()
			throws ServletException, IOException, SoyException {
		String baseUrl = "baseurl";
		String defaultUser = "defaultuser";
		String defaultToken = "defaulttoken";
		when(req.getPathInfo()).thenReturn(GLOBAL_PATH);
		when(req.getParameter("jenkinsUrl")).thenReturn(baseUrl);
		when(req.getParameter("jenkinsUser")).thenReturn(defaultUser);
		when(req.getParameter("jenkinsToken")).thenReturn(defaultToken);
		when(req.getParameter("jenkinsAltUrl")).thenReturn(null);
		when(req.getParameter("jenkinsCSRF")).thenReturn(null);
		servlet.doPost(req, resp);

		Server expected = new Server(baseUrl, defaultUser, defaultToken, false, false);
		verify(jenkins, times(1)).saveJenkinsServer(expected);
	}

	// @Test
	public void testDoPostGlobalSettingsAltUrlFalse()
			throws ServletException, IOException, SoyException {

		String[] baseUrl = {"baseurl"};
		String[] defaultUser = {"defaultuser"};
		String[] defaultToken = {"defaulttoken"};
		String[] altUrl = {""};
		String[] csrfEnabled = {"on"};
		String saveButton = "'Save Connection";
		Map<String, String[]> params = ImmutableMap.<String, String[]>builder()
				.put("jenkinsUrl", baseUrl)
				.put("jenkinsUser", defaultUser)
				.put("jenkinsToken",  defaultToken)
				.put("jenkinsAltUrl", altUrl)
				.put("jenkinsCSRF", csrfEnabled)
				.build();

		when(req.getParameterMap()).thenReturn(params);
		when(req.getPathInfo()).thenReturn(GLOBAL_PATH);
		when(req.getParameter("submit")).thenReturn(saveButton);
		servlet.doPost(req, resp);

		Server expected = new Server(baseUrl[0], defaultUser[0], defaultToken[0], false, false);
		verify(jenkins, times(1)).saveJenkinsServer(expected);
	}

	@Test
	public void testDoPostGlobalSettingsClear() throws ServletException, IOException, SoyException {

		String[] baseUrl = {"baseurl"};
		String[] defaultUser = {"defaultuser"};
		String[] defaultToken = {"defaulttoken"};
		String[] altUrl = {"on"};
		String[] csrfEnabled = {"on"};
		String clear = "on";
		String saveButton = "'Save Connection";
		Map<String, String[]> params = ImmutableMap.<String, String[]>builder()
				.put("jenkinsUrl", baseUrl)
				.put("jenkinsUser", defaultUser)
				.put("jenkinsToken",  defaultToken)
				.put("jenkinsAltUrl", altUrl)
				.put("jenkinsCSRF", csrfEnabled)
				.build();

		when(req.getParameterMap()).thenReturn(params);
		when(req.getPathInfo()).thenReturn(GLOBAL_PATH);
		when(req.getParameter("clear-settings")).thenReturn(clear);
		when(req.getParameter("submit")).thenReturn(saveButton);
		servlet.doPost(req, resp);

		verify(jenkins, times(1)).saveJenkinsServer(null);
	}

	@Test
	public void testDoPostGlobalSettingsClearFalse()
			throws ServletException, IOException, SoyException {
		String[] baseUrl = {"baseurl"};
		String[] defaultUser = {"defaultuser"};
		String[] defaultToken = {"defaulttoken"};
		String[] altUrl = {"on"};
		String[] csrfEnabled = {"on"};
		String clear = "";
		String saveButton = "'Save Connection";
		Map<String, String[]> params = ImmutableMap.<String, String[]>builder()
				.put("jenkinsUrl", baseUrl)
				.put("jenkinsUser", defaultUser)
				.put("jenkinsToken",  defaultToken)
				.put("jenkinsAltUrl", altUrl)
				.put("jenkinsCSRF", csrfEnabled)
				.build();

		when(req.getParameterMap()).thenReturn(params);
		when(req.getPathInfo()).thenReturn(GLOBAL_PATH);
		when(req.getParameter("clear-settings")).thenReturn(clear);
		when(req.getParameter("submit")).thenReturn(saveButton);
		servlet.doPost(req, resp);

		verify(jenkins, times(0)).saveJenkinsServer(null);
	}

	@Test
	public void testDoPostGlobalSettingsClearNull()
			throws ServletException, IOException, SoyException {
		String[] baseUrl = {"baseurl"};
		String[] defaultUser = {"defaultuser"};
		String[] defaultToken = {"defaulttoken"};
		String[] altUrl = {"on"};
		String[] csrfEnabled = {"on"};
		String saveButton = "'Save Connection";
		Map<String, String[]> params = ImmutableMap.<String, String[]>builder()
				.put("jenkinsUrl", baseUrl)
				.put("jenkinsUser", defaultUser)
				.put("jenkinsToken",  defaultToken)
				.put("jenkinsAltUrl", altUrl)
				.put("jenkinsCSRF", csrfEnabled)
				.build();

		when(req.getParameterMap()).thenReturn(params);
		when(req.getPathInfo()).thenReturn(GLOBAL_PATH);
		when(req.getParameter("clear-settings")).thenReturn(null);
		when(req.getParameter("submit")).thenReturn(saveButton);
		servlet.doPost(req, resp);

		verify(jenkins, times(0)).saveJenkinsServer(null);
	}

	@Test
	public void testDoPostProjectSettings() throws ServletException, IOException, SoyException {
		String[] baseUrl = {"baseurl"};
		String[] defaultUser = {"defaultuser"};
		String[] defaultToken = {"defaulttoken"};
		String[] altUrl = {"on"};
		String[] csrfEnabled = {"on"};
		String saveButton = "'Save Connection";
		Map<String, String[]> params = ImmutableMap.<String, String[]>builder()
				.put("jenkinsUrl", baseUrl)
				.put("jenkinsUser", defaultUser)
				.put("jenkinsToken",  defaultToken)
				.put("jenkinsAltUrl", altUrl)
				.put("jenkinsCSRF", csrfEnabled)
				.build();

		when(req.getParameterMap()).thenReturn(params);
		when(req.getPathInfo()).thenReturn(PROJECT_PATH + PROJECT_KEY);
		when(req.getParameter("submit")).thenReturn(saveButton);
		servlet.doPost(req, resp);

		verify(jenkins, times(1))
				.saveJenkinsServer(isNotNull(Server.class), isNotNull(String.class));
		verify(renderer, times(1)).render(any(), any(), any(), any());
	}

	@Test
	public void testDoPostProjectSettingsEmptyUrl()
			throws ServletException, IOException, SoyException {
		String[] baseUrl = {""};
		String[] defaultUser = {"defaultuser"};
		String[] defaultToken = {"defaulttoken"};
		String[] altUrl = {"on"};
		String[] csrfEnabled = {"on"};
		String saveButton = "'Save Connection";
		Map<String, String[]> params = ImmutableMap.<String, String[]>builder()
				.put("jenkinsUrl", baseUrl)
				.put("jenkinsUser", defaultUser)
				.put("jenkinsToken",  defaultToken)
				.put("jenkinsAltUrl", altUrl)
				.put("jenkinsCSRF", csrfEnabled)
				.build();

		when(req.getParameterMap()).thenReturn(params);
		when(req.getPathInfo()).thenReturn(PROJECT_PATH + PROJECT_KEY);
		when(req.getParameter("submit")).thenReturn(saveButton);
		servlet.doPost(req, resp);

		verify(renderer, times(1)).render(any(), any(), any(), any());
	}

	@Test
	public void testDoPostProjectSettingsClear()
			throws ServletException, IOException, SoyException {

		String[] baseUrl = {""};
		String[] defaultUser = {"defaultuser"};
		String[] defaultToken = {"defaulttoken"};
		String[] altUrl = {"on"};
		String[] csrfEnabled = {"on"};
		String clear = "on";
		String saveButton = "'Save Connection";
		Map<String, String[]> params = ImmutableMap.<String, String[]>builder()
				.put("jenkinsUrl", baseUrl)
				.put("jenkinsUser", defaultUser)
				.put("jenkinsToken",  defaultToken)
				.put("jenkinsAltUrl", altUrl)
				.put("jenkinsCSRF", csrfEnabled)
				.build();

		when(req.getParameterMap()).thenReturn(params);
		when(req.getPathInfo()).thenReturn(PROJECT_PATH + PROJECT_KEY);
		when(req.getParameter("clear-settings")).thenReturn(clear);
		when(req.getParameter("submit")).thenReturn(saveButton);
		servlet.doPost(req, resp);

		verify(jenkins, times(1)).saveJenkinsServer(null, PROJECT_KEY);
	}

	@Test
	public void testDoPostProjectSettingsClearFalse()
			throws ServletException, IOException, SoyException {

		String[] baseUrl = {"baseurl"};
		String[] defaultUser = {"defaultuser"};
		String[] defaultToken = {"defaulttoken"};
		String[] altUrl = {"on"};
		String[] csrfEnabled = {"on"};
		String clear = "";
		String saveButton = "'Save Connection";
		Map<String, String[]> params = ImmutableMap.<String, String[]>builder()
				.put("jenkinsUrl", baseUrl)
				.put("jenkinsUser", defaultUser)
				.put("jenkinsToken",  defaultToken)
				.put("jenkinsAltUrl", altUrl)
				.put("jenkinsCSRF", csrfEnabled)
				.build();

		when(req.getParameterMap()).thenReturn(params);
		when(req.getPathInfo()).thenReturn(PROJECT_PATH + PROJECT_KEY);
		when(req.getParameter("clear-settings")).thenReturn(clear);
		when(req.getParameter("submit")).thenReturn(saveButton);
		servlet.doPost(req, resp);

		verify(jenkins, times(0)).saveJenkinsServer(null, PROJECT_KEY);
	}
}
