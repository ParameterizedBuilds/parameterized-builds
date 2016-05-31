package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.google.common.collect.ImmutableMap;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

public class CIServletTest {
	private static final String SOY_TEMPLATE = "com.kylenicholls.stash.parameterized-builds:jenkins-admin-soy";
	private CIServlet servlet;
	private SoyTemplateRenderer renderer;
	private Jenkins jenkins;
	private AuthenticationContext authContext;
	private HttpServletRequest req;
	private HttpServletResponse resp;
	private ApplicationUser user;

	@Before
	public void setup() throws IOException {
		renderer = mock(SoyTemplateRenderer.class);
		authContext = mock(AuthenticationContext.class);
		NavBuilder navBuilder = mock(NavBuilder.class);
		jenkins = mock(Jenkins.class);
		servlet = new CIServlet(renderer, authContext, navBuilder, jenkins);

		req = mock(HttpServletRequest.class);
		resp = mock(HttpServletResponse.class);
		user = mock(ApplicationUser.class);
		when(authContext.isAuthenticated()).thenReturn(true);
		when(authContext.getCurrentUser()).thenReturn(user);
	}

	@Test
	public void testGetServerSettings() throws ServletException, IOException, SoyException {
		when(req.getPathInfo()).thenReturn("/plugins/servlet/jenkins");
		Server server = new Server("baseurl", null, null, false);
		when(jenkins.getSettings()).thenReturn(server);
		servlet.doGet(req, resp);

		Map<String, Object> data = ImmutableMap.<String, Object> of("server", server, "errors", "");
		verify(renderer, times(1))
				.render(resp.getWriter(), SOY_TEMPLATE, "jenkins.admin.settings", data);
	}

	@Test
	public void testGetServerSettingsNull() throws ServletException, IOException, SoyException {
		when(req.getPathInfo()).thenReturn("/plugins/servlet/jenkins");
		when(jenkins.getSettings()).thenReturn(null);
		servlet.doGet(req, resp);

		Map<String, Object> data = ImmutableMap.<String, Object> of("server", "", "errors", "");
		verify(renderer, times(1))
				.render(resp.getWriter(), SOY_TEMPLATE, "jenkins.admin.settings", data);
	}

	@Test
	public void testGetAccountSettingsAndNoJenkinsSettings()
			throws ServletException, IOException, SoyException {
		when(req.getPathInfo()).thenReturn("/plugins/servlet/account/jenkins");
		when(jenkins.getSettings()).thenReturn(null);
		servlet.doGet(req, resp);

		Map<String, Object> data = ImmutableMap
				.<String, Object> of("user", user, "token", "", "baseUrl", "", "errors", "A Bitbucket administrator must configure the base settings for Jenkins first. These settings can be found on the admin page of Bitbucket.");
		verify(renderer, times(1))
				.render(resp.getWriter(), SOY_TEMPLATE, "jenkins.user.settings", data);
	}

	@Test
	public void testGetAccountSettingsNull() throws ServletException, IOException, SoyException {
		when(req.getPathInfo()).thenReturn("/plugins/servlet/account/jenkins");
		Server server = new Server("baseurl", null, null, false);
		when(jenkins.getSettings()).thenReturn(server);
		when(jenkins.getUserSettings(user)).thenReturn(null);
		servlet.doGet(req, resp);

		Map<String, Object> data = ImmutableMap
				.<String, Object> of("user", user, "token", "", "baseUrl", server
						.getBaseUrl(), "errors", "");
		verify(renderer, times(1))
				.render(resp.getWriter(), SOY_TEMPLATE, "jenkins.user.settings", data);
	}

	@Test
	public void testGetAccountSettings() throws ServletException, IOException, SoyException {
		when(req.getPathInfo()).thenReturn("/plugins/servlet/account/jenkins");
		Server server = new Server("baseurl", null, null, false);
		when(jenkins.getSettings()).thenReturn(server);
		when(jenkins.getUserSettings(user)).thenReturn("token");
		servlet.doGet(req, resp);

		Map<String, Object> data = ImmutableMap
				.<String, Object> of("user", user, "token", "token", "baseUrl", server
						.getBaseUrl(), "errors", "");
		verify(renderer, times(1))
				.render(resp.getWriter(), SOY_TEMPLATE, "jenkins.user.settings", data);
	}

	@Test
	public void testPostAccountSettings() throws ServletException, IOException, SoyException {
		when(req.getPathInfo()).thenReturn("/plugins/servlet/account/jenkins");
		when(req.getParameter("jenkinsToken")).thenReturn("token");
		servlet.doPost(req, resp);

		verify(jenkins, times(1)).setUserSettings(user, "token");
	}

	@Test
	public void testPostJenkinsSettings() throws ServletException, IOException, SoyException {
		String baseUrl = "baseurl";
		String defaultUser = "defaultuser";
		String defaultToken = "defaulttoken";
		String altUrl = "on";
		when(req.getPathInfo()).thenReturn("/plugins/servlet/jenkins");
		when(req.getParameter("jenkinsUrl")).thenReturn(baseUrl);
		when(req.getParameter("jenkinsUser")).thenReturn(defaultUser);
		when(req.getParameter("jenkinsToken")).thenReturn(defaultToken);
		when(req.getParameter("jenkinsAltUrl")).thenReturn(altUrl);
		servlet.doPost(req, resp);

		verify(jenkins, times(1)).setSettings(baseUrl, defaultUser, defaultToken, true);
	}

	@Test
	public void testPostJenkinsSettingsAltUrlNull()
			throws ServletException, IOException, SoyException {
		String baseUrl = "baseurl";
		String defaultUser = "defaultuser";
		String defaultToken = "defaulttoken";
		when(req.getPathInfo()).thenReturn("/plugins/servlet/jenkins");
		when(req.getParameter("jenkinsUrl")).thenReturn(baseUrl);
		when(req.getParameter("jenkinsUser")).thenReturn(defaultUser);
		when(req.getParameter("jenkinsToken")).thenReturn(defaultToken);
		when(req.getParameter("jenkinsAltUrl")).thenReturn(null);
		servlet.doPost(req, resp);

		verify(jenkins, times(1)).setSettings(baseUrl, defaultUser, defaultToken, false);
	}

	@Test
	public void testPostJenkinsSettingsAltUrlFalse()
			throws ServletException, IOException, SoyException {
		String baseUrl = "baseurl";
		String defaultUser = "defaultuser";
		String defaultToken = "defaulttoken";
		String altUrl = "";
		when(req.getPathInfo()).thenReturn("/plugins/servlet/jenkins");
		when(req.getParameter("jenkinsUrl")).thenReturn(baseUrl);
		when(req.getParameter("jenkinsUser")).thenReturn(defaultUser);
		when(req.getParameter("jenkinsToken")).thenReturn(defaultToken);
		when(req.getParameter("jenkinsAltUrl")).thenReturn(altUrl);
		servlet.doPost(req, resp);

		verify(jenkins, times(1)).setSettings(baseUrl, defaultUser, defaultToken, false);
	}

	@Test
	public void testPostJenkinsSettingsClearTrue()
			throws ServletException, IOException, SoyException {
		String clear = "on";
		when(req.getPathInfo()).thenReturn("/plugins/servlet/jenkins");
		when(req.getParameter("clear-settings")).thenReturn(clear);
		servlet.doPost(req, resp);

		verify(jenkins, times(1)).setSettings("", "", "", false);
	}

	@Test
	public void testPostJenkinsSettingsClearFalse()
			throws ServletException, IOException, SoyException {
		String baseUrl = "baseurl";
		String clear = "";
		when(req.getPathInfo()).thenReturn("/plugins/servlet/jenkins");
		when(req.getParameter("jenkinsUrl")).thenReturn(baseUrl);
		when(req.getParameter("clear-settings")).thenReturn(clear);
		servlet.doPost(req, resp);

		verify(jenkins, times(1)).setSettings(baseUrl, null, null, false);
	}

	@Test
	public void testPostJenkinsSettingsEmtpyUrl()
			throws ServletException, IOException, SoyException {
		String baseUrl = "";
		String defaultUser = "";
		String defaultToken = "";
		when(req.getPathInfo()).thenReturn("/plugins/servlet/jenkins");
		when(req.getParameter("jenkinsUrl")).thenReturn(baseUrl);
		when(req.getParameter("jenkinsUser")).thenReturn(defaultUser);
		when(req.getParameter("jenkinsToken")).thenReturn(defaultToken);
		servlet.doPost(req, resp);

		verify(jenkins, times(0)).setSettings(baseUrl, defaultUser, defaultToken, false);
	}
}
