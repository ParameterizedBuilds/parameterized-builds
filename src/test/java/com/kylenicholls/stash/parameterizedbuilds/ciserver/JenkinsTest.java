package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

public class JenkinsTest {
	private static final String PLUGIN_KEY = "com.kylenicholls.stash.parameterized-builds";
	private static final String USER_SLUG = "slug";
	private static final String JOB_NAME = "jobname";
	private Jenkins jenkins;
	private PluginSettings pluginSettings;
	private ApplicationUser user;

	@Before
	public void setup() throws IOException {
		PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
		pluginSettings = mock(PluginSettings.class);
		when(factory.createSettingsForKey(PLUGIN_KEY)).thenReturn(pluginSettings);
		jenkins = new Jenkins(factory);

		user = mock(ApplicationUser.class);
		when(user.getSlug()).thenReturn(USER_SLUG);
	}

	@Test
	public void testRemoveSettingsWithNullUrl() {
		jenkins.setSettings(null, null, null, false);
		verify(pluginSettings, times(1)).remove(".jenkinsSettings");
	}

	@Test
	public void testRemoveSettingsWithEmptyUrl() {
		jenkins.setSettings("", null, null, false);
		verify(pluginSettings, times(1)).remove(".jenkinsSettings");
	}

	@Test
	public void testSetSettingsWithValues() {
		String url = "url";
		String user = "user";
		String token = "token";
		boolean altUrl = true;
		jenkins.setSettings(url, user, token, altUrl);
		verify(pluginSettings, times(1))
				.put(".jenkinsSettings", url + ";" + user + ";" + token + ";true");
	}

	@Test
	public void testSetSettingsWithFalseAltUrl() {
		String url = "url";
		String user = "user";
		String token = "token";
		boolean altUrl = false;
		jenkins.setSettings(url, user, token, altUrl);
		verify(pluginSettings, times(1))
				.put(".jenkinsSettings", url + ";" + user + ";" + token + ";false");
	}

	@Test
	public void testSetUserSettingsWithNullUser() {
		jenkins.setUserSettings(null, "");
		verify(pluginSettings, times(0)).put(any(), any());
	}

	@Test
	public void testRemoveUserSettingsWithNullToken() {
		jenkins.setUserSettings(user, null);
		verify(pluginSettings, times(1)).remove(".jenkinsUser." + USER_SLUG);
	}

	@Test
	public void testRemoveUserSettingsWithEmptyToken() {
		jenkins.setUserSettings(user, "");
		verify(pluginSettings, times(1)).remove(".jenkinsUser." + USER_SLUG);
	}

	@Test
	public void testSetUserSettingsWithToken() {
		String token = "token";
		jenkins.setUserSettings(user, token);
		verify(pluginSettings, times(1)).put(".jenkinsUser." + USER_SLUG, token);
	}

	@Test
	public void testGetSettingsNull() {
		when(pluginSettings.get(PLUGIN_KEY)).thenReturn(null);
		Server server = jenkins.getSettings();
		assertEquals(null, server);
	}

	@Test
	public void testGetSettingsNoAltUrl() {
		when(pluginSettings.get(".jenkinsSettings")).thenReturn("url;user;token");
		Server server = jenkins.getSettings();
		assertEquals("url", server.getBaseUrl());
		assertEquals("user", server.getUser());
		assertEquals("token", server.getToken());
		assertFalse(server.getAltUrl());
	}

	@Test
	public void testGetSettingsWithAltUrlTrue() {
		when(pluginSettings.get(".jenkinsSettings")).thenReturn("url;user;token;true");
		Server server = jenkins.getSettings();
		assertEquals("url", server.getBaseUrl());
		assertEquals("user", server.getUser());
		assertEquals("token", server.getToken());
		assertTrue(server.getAltUrl());
	}

	@Test
	public void testGetSettingsWithAltUrlFalse() {
		when(pluginSettings.get(".jenkinsSettings")).thenReturn("url;user;token;false");
		Server server = jenkins.getSettings();
		assertEquals("url", server.getBaseUrl());
		assertEquals("user", server.getUser());
		assertEquals("token", server.getToken());
		assertFalse(server.getAltUrl());
	}

	@Test
	public void testGetUserTokenWithTokenSet() {
		String token = "token";
		when(pluginSettings.get(".jenkinsUser." + USER_SLUG)).thenReturn("token");
		String userToken = jenkins.getUserToken(user);
		assertEquals(USER_SLUG + ":" + token, userToken);
	}

	@Test
	public void testGetUserTokenNullUser() {
		String userToken = jenkins.getUserToken(null);
		assertEquals(null, userToken);
	}

	@Test
	public void testGetUserTokenWithTokenNull() {
		when(pluginSettings.get(".jenkinsUser." + USER_SLUG)).thenReturn(null);
		String userToken = jenkins.getUserToken(user);
		assertEquals(null, userToken);
	}

	@Test
	public void testGetUserSettingsNull() {
		String userToken = jenkins.getUserSettings(null);
		assertEquals(null, userToken);
	}

	@Test
	public void testTriggerJobWithNoServerSettings() {
		when(pluginSettings.get(".jenkinsSettings")).thenReturn(null);
		Job job = new Job.JobBuilder(1).jobName(JOB_NAME).createJob();
		String[] results = jenkins.triggerJob(job, "", "");
		assertEquals("error", results[0]);
		assertEquals("Jenkins settings are not setup", results[1]);
	}
}
