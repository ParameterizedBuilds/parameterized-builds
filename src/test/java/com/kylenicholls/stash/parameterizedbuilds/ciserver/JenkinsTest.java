package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.kylenicholls.stash.parameterizedbuilds.item.*;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class JenkinsTest {
	private static final String PLUGIN_KEY = "com.kylenicholls.stash.parameterized-builds";
	private static final String USER_SLUG = "slug";
	private static final String PROJECT_KEY = "projkey";
	private Jenkins jenkins;
	private PluginSettings pluginSettings;
	private ApplicationUser user;
	private Project project;
	private ProjectService projectService;

	@Before
	public void setup() throws IOException {
		PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
		pluginSettings = mock(PluginSettings.class);
		when(factory.createSettingsForKey(PLUGIN_KEY)).thenReturn(pluginSettings);
		jenkins = new Jenkins(factory);

		user = mock(ApplicationUser.class);
		when(user.getSlug()).thenReturn(USER_SLUG);
		project = mock(Project.class);
		when(project.getKey()).thenReturn(PROJECT_KEY);
		projectService = mock(ProjectService.class);
	}

	@Test
	public void testSaveGlobalJenkinsServerNull() {
		jenkins.saveJenkinsServer(null, null);

		verify(pluginSettings, times(1)).remove(".jenkinsSettings");
	}

	@Test
	public void testSaveProjectJenkinsServerNull() {
		jenkins.saveJenkinsServer(null, PROJECT_KEY);

		verify(pluginSettings, times(1)).remove(".jenkinsSettings." + PROJECT_KEY);
	}

	@Test
	public void testSaveGlobalJenkinsServer() {
		Server server = new Server("url", null, "user", "token", true, true);
		jenkins.saveJenkinsServer(server, null);

		verify(pluginSettings, times(1)).put(".jenkinsSettings", server.asMap());
	}

	@Test
	public void testSaveProjectJenkinsServer() {
		Server server = new Server("url", null, "user", "token", true, true);
		jenkins.saveJenkinsServer(server, PROJECT_KEY);

		verify(pluginSettings, times(1)).put(".jenkinsSettings." + PROJECT_KEY, server.asMap());
	}

	@Test
	public void testSaveGlobalUserTokenWithEmptyToken() {
		jenkins.saveUserToken(USER_SLUG, "", "");

		verify(pluginSettings, times(1)).remove(".jenkinsUser." + USER_SLUG);
	}

	@Test
	public void testSaveProjectUserTokenWithEmptyToken() {
		jenkins.saveUserToken(USER_SLUG, PROJECT_KEY, "");

		verify(pluginSettings, times(1)).remove(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY);
	}

	@Test
	public void testSaveGlobalUserToken() {
		String token = "token";
		jenkins.saveUserToken(USER_SLUG, "", token);

		verify(pluginSettings, times(1)).put(".jenkinsUser." + USER_SLUG, token);
	}

	@Test
	public void testSaveProjectUserToken() {
		String token = "token";
		jenkins.saveUserToken(USER_SLUG, PROJECT_KEY, token);

		verify(pluginSettings, times(1))
				.put(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY, token);
	}

	@Test
	public void testGetJenkinsServerSettingsNull() {
		when(pluginSettings.get(".jenkinsSettings")).thenReturn(null);
		Server actual = jenkins.getJenkinsServer(null);

		assertEquals(null, actual);
	}

	@Test
	public void testGetJenkinsServerSettingsLegacyNoAltUrl() {
		when(pluginSettings.get(".jenkinsSettings")).thenReturn("url;user;token");
		Server actual = jenkins.getJenkinsServer(null);

		assertEquals("url", actual.getBaseUrl());
		assertEquals("user", actual.getUser());
		assertEquals("token", actual.getToken());
		assertFalse(actual.getAltUrl());
	}

	@Test
	public void testGetJenkinsServerSettingsLegacyAltUrlTrue() {
		when(pluginSettings.get(".jenkinsSettings")).thenReturn("url;user;token;true");
		Server actual = jenkins.getJenkinsServer(null);

		assertEquals("url", actual.getBaseUrl());
		assertEquals("user", actual.getUser());
		assertEquals("token", actual.getToken());
		assertTrue(actual.getAltUrl());
	}

	@Test
	public void testGetJenkinsServerSettingsLegacyAltUrlFalse() {
		when(pluginSettings.get(".jenkinsSettings")).thenReturn("url;user;token;false");
		Server actual = jenkins.getJenkinsServer(null);

		assertEquals("url", actual.getBaseUrl());
		assertEquals("user", actual.getUser());
		assertEquals("token", actual.getToken());
		assertFalse(actual.getAltUrl());
	}

	@Test
	public void testGetJenkinsServerSettings() {
		Server expected = new Server("url", null, "user", "token", false, false);
		when(pluginSettings.get(".jenkinsSettings")).thenReturn(expected.asMap());
		Server actual = jenkins.getJenkinsServer(null);

		assertEquals(expected.asMap(), actual.asMap());
	}

	@Test
	public void testGetProjectServerSettingsNull() {
		when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(null);
		Server actual = jenkins.getJenkinsServer(PROJECT_KEY);

		assertEquals(null, actual);
	}

	@Test
	public void testGetProjectServerSettings() {
		Server expected = new Server("url", null, "user", "token", false, false);
		when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());
		Server actual = jenkins.getJenkinsServer(PROJECT_KEY);

		assertEquals(expected.asMap(), actual.asMap());
	}

	@Test
	public void testGetAllUserTokensWithGlobalServer() {
		Server globalServer = new Server("globalUrl", null, "globaluser", "globaltoken", false, false);
		String token = "token";
		List<String> projectKeys = new ArrayList<>();
		when(pluginSettings.get(".jenkinsSettings")).thenReturn(globalServer.asMap());
		when(pluginSettings.get(".jenkinsUser." + USER_SLUG)).thenReturn(token);
		List<UserToken> actual = jenkins.getAllUserTokens(user, projectKeys, projectService);

		assertEquals(1, actual.size());
		assertEquals(globalServer.getBaseUrl(), actual.get(0).getBaseUrl());
		assertEquals("", actual.get(0).getProjectKey());
		assertEquals("Global", actual.get(0).getProjectName());
		assertEquals(USER_SLUG, actual.get(0).getUserSlug());
		assertEquals(token, actual.get(0).getToken());
	}

	@Test
	public void testGetAllUserTokensWithGlobalServerNull() {
		List<String> projectKeys = new ArrayList<>();
		when(pluginSettings.get(".jenkinsSettings")).thenReturn(null);
		List<UserToken> actual = jenkins.getAllUserTokens(user, projectKeys, projectService);

		assertEquals(0, actual.size());
	}

	@Test
	public void testGetAllUserTokensProjectServerNull() {
		Server globalServer = new Server("globalUrl", null, "globaluser", "globaltoken", false, false);
		String newProjectKey = "newkey";
		List<String> projectKeys = new ArrayList<>();
		projectKeys.add(newProjectKey);
		when(pluginSettings.get(".jenkinsSettings")).thenReturn(globalServer.asMap());
		when(pluginSettings.get(".jenkinsSettings." + newProjectKey)).thenReturn(null);
		List<UserToken> actual = jenkins.getAllUserTokens(user, projectKeys, projectService);

		assertEquals(1, actual.size());
	}

	@Test
	public void testGetAllUserTokensProjectServerTokenNull() {
		Server globalServer = new Server("globalUrl", null, "globaluser", "globaltoken", false, false);
		String newProjectKey = "newkey";
		String newProjectName = "newName";
		List<String> projectKeys = new ArrayList<>();
		projectKeys.add(newProjectKey);
		when(pluginSettings.get(".jenkinsSettings")).thenReturn(globalServer.asMap());
		when(projectService.getByKey(newProjectKey)).thenReturn(project);
		when(project.getName()).thenReturn(newProjectName);
		Server projectServer = new Server("newbaseurl", null, "newuser", "newtoken", false, false);
		when(pluginSettings.get(".jenkinsSettings." + newProjectKey))
				.thenReturn(projectServer.asMap());
		List<UserToken> actual = jenkins.getAllUserTokens(user, projectKeys, projectService);

		assertEquals(2, actual.size());
		assertEquals(projectServer.getBaseUrl(), actual.get(1).getBaseUrl());
		assertEquals(newProjectKey, actual.get(1).getProjectKey());
		assertEquals(newProjectName, actual.get(1).getProjectName());
		assertEquals(USER_SLUG, actual.get(1).getUserSlug());
		assertEquals(null, actual.get(1).getToken());
	}

	@Test
	public void testGetAllUserTokensProjectServer() {
		Server globalServer = new Server("globalUrl", null, "globaluser", "globaltoken", false, false);
		String newProjectKey = "newkey";
		String newProjectName = "newName";
		String token = "token";
		List<String> projectKeys = new ArrayList<>();
		projectKeys.add(newProjectKey);
		when(pluginSettings.get(".jenkinsSettings")).thenReturn(globalServer.asMap());
		when(projectService.getByKey(newProjectKey)).thenReturn(project);
		when(project.getName()).thenReturn(newProjectName);
		when(pluginSettings.get(".jenkinsUser." + USER_SLUG + "." + newProjectKey))
				.thenReturn(token);
		Server projectServer = new Server("newbaseurl", null, "newuser", "newtoken", false, false);
		when(pluginSettings.get(".jenkinsSettings." + newProjectKey))
				.thenReturn(projectServer.asMap());
		List<UserToken> actual = jenkins.getAllUserTokens(user, projectKeys, projectService);

		assertEquals(2, actual.size());
		assertEquals(token, actual.get(1).getToken());
	}

	@Test
	public void testTriggerJobUseJobServer(){
		String userToken = USER_SLUG + ":token";
		String userCSRF = null;
		Server expected = new Server("globalurl", null, user.getDisplayName(), "token", false, false);
		when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());
		when(pluginSettings.get(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY)).thenReturn("token");

		Job job = new Job.JobBuilder(1).jobName("").buildParameters("").branchRegex("").jenkinsServer(PROJECT_KEY)
				.pathRegex("").prDestRegex("").build();
		BitbucketVariables bitbucketVariables = new BitbucketVariables.Builder().add("$TRIGGER", () -> Job.Trigger.ADD.toString()).build();
		Jenkins jenkinsSpy = spy(jenkins);
		jenkinsSpy.triggerJob(PROJECT_KEY, user, job, bitbucketVariables);

		verify(jenkinsSpy, times(1)).sanitizeTrigger("globalurl/job/build", userToken, userCSRF, false);
	}

	@Test
	public void testTriggerJobUseJobServerGlobal(){
		String userToken = USER_SLUG + ":token";
		String userCSRF = null;
		Server expected = new Server("globalurl", null, user.getDisplayName(), "token", false, false);
		when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());
		when(pluginSettings.get(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY)).thenReturn("token");

		Job job = new Job.JobBuilder(1).jobName("").buildParameters("").branchRegex("").jenkinsServer(PROJECT_KEY)
				.pathRegex("").prDestRegex("").build();
		BitbucketVariables bitbucketVariables = new BitbucketVariables.Builder().add("$TRIGGER", () -> Job.Trigger.ADD.toString()).build();
		Jenkins jenkinsSpy = spy(jenkins);
		jenkinsSpy.triggerJob(PROJECT_KEY, user, job, bitbucketVariables);

		verify(jenkinsSpy, times(1)).sanitizeTrigger("globalurl/job/build", userToken, userCSRF, false);
	}

	@Test
	public void testTriggerJobUseProjectServerAndUserToken(){
		String userToken = USER_SLUG + ":token";
		String userCSRF = null;
		Server expected = new Server("globalurl", null, user.getDisplayName(), "token", false, false);
		when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());
		when(pluginSettings.get(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY)).thenReturn("token");

		Job job = new Job.JobBuilder(1).jobName("").buildParameters("").branchRegex("")
				.pathRegex("").prDestRegex("").build();
		BitbucketVariables bitbucketVariables = new BitbucketVariables.Builder().add("$TRIGGER", () -> Job.Trigger.ADD.toString()).build();
		Jenkins jenkinsSpy = spy(jenkins);
		jenkinsSpy.triggerJob(PROJECT_KEY, user, job, bitbucketVariables);

		verify(jenkinsSpy, times(1)).sanitizeTrigger("globalurl/job/build", userToken, userCSRF, false);
	}

	@Test
	public void testTriggerJobUseGlobalJenkinsAndUserToken(){
		String userToken = USER_SLUG + ":token";
		String userCSRF = null;
		Server expected = new Server("globalurl", null, user.getDisplayName(), "token", false, false);
		when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(null);
		when(pluginSettings.get(".jenkinsSettings")).thenReturn(expected.asMap());
		when(pluginSettings.get(".jenkinsUser." + USER_SLUG)).thenReturn("token");

		Job job = new Job.JobBuilder(1).jobName("").buildParameters("").branchRegex("")
				.pathRegex("").prDestRegex("").build();
		BitbucketVariables bitbucketVariables =  new BitbucketVariables.Builder().add("$TRIGGER", () -> Job.Trigger.ADD.toString()).build();;
		Jenkins jenkinsSpy = spy(jenkins);
		jenkinsSpy.triggerJob(PROJECT_KEY, user, job, bitbucketVariables);

		verify(jenkinsSpy, times(1)).sanitizeTrigger("globalurl/job/build", userToken, userCSRF, false);
	}

	@Test
	public void testTriggerJobNoProjectUserSet(){
		String userToken = USER_SLUG + ":token";
		String userCSRF = null;
		when(user.getDisplayName()).thenReturn(USER_SLUG);
		Server expected = new Server("globalurl", null, user.getDisplayName(), "token", false, false);
		when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());

		Job job = new Job.JobBuilder(1).jobName("").buildParameters("").branchRegex("")
				.pathRegex("").prDestRegex("").build();
		BitbucketVariables bitbucketVariables =  new BitbucketVariables.Builder().add("$TRIGGER", () -> Job.Trigger.ADD.toString()).build();;
		Jenkins jenkinsSpy = spy(jenkins);
		jenkinsSpy.triggerJob(PROJECT_KEY, user, job, bitbucketVariables);

		verify(jenkinsSpy, times(1)).sanitizeTrigger("globalurl/job/build", userToken, userCSRF, true);
	}

	@Test
	public void testTriggerJobNoDefaultUserSet(){
		when(user.getDisplayName()).thenReturn("user");
		Server expected = new Server("globalurl", "", "", "", false, false);
		when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());

		Job job = new Job.JobBuilder(1).jobName("").buildParameters("").branchRegex("")
				.pathRegex("").prDestRegex("").build();
		BitbucketVariables bitbucketVariables = new BitbucketVariables.Builder().add("$TRIGGER", () -> Job.Trigger.ADD.toString()).build();
		Jenkins jenkinsSpy = spy(jenkins);
		jenkinsSpy.triggerJob(PROJECT_KEY, user, job, bitbucketVariables);

		verify(jenkinsSpy, times(1)).sanitizeTrigger("globalurl/job/build", null, null, true);
	}

	@Test
	public void testTriggerJobNoBuildUrl() {
		JenkinsResponse actual = jenkins.sanitizeTrigger(null, null, null, false);

		assertEquals(true, actual.getError());
		assertEquals(false, actual.getPrompt());
		assertEquals("Jenkins settings are not setup", actual.getMessageText());
	}

	@Test
	public void testGetJoinedGlobalToken() {
		String token = "token";
		when(pluginSettings.get(".jenkinsUser." + USER_SLUG)).thenReturn("token");
		String actual = jenkins.getJoinedUserToken(user, null);

		assertEquals(USER_SLUG + ":" + token, actual);
	}

	@Test
	public void testGetJoinedGlobalTokenNullToken() {
		when(pluginSettings.get(".jenkinsUser." + USER_SLUG)).thenReturn(null);
		String actual = jenkins.getJoinedUserToken(user, null);

		assertEquals(null, actual);
	}

	@Test
	public void testGetJoinedGlobalTokenNullUser() {
		String actual = jenkins.getJoinedUserToken(null, null);

		assertEquals(null, actual);
	}

	@Test
	public void testGetJoinedProjectToken() {
		String token = "token";
		when(pluginSettings.get(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY))
				.thenReturn("token");
		String actual = jenkins.getJoinedUserToken(user, PROJECT_KEY);

		assertEquals(USER_SLUG + ":" + token, actual);
	}

	@Test
	public void testGetJoinedProjectTokenNullToken() {
		when(pluginSettings.get(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY)).thenReturn(null);
		String actual = jenkins.getJoinedUserToken(user, PROJECT_KEY);

		assertEquals(null, actual);
	}

	@Test
	public void testGetJoinedProjectTokenNullUser() {
		String actual = jenkins.getJoinedUserToken(null, PROJECT_KEY);

		assertEquals(null, actual);
	}
}
