package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base64;

import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.kylenicholls.stash.parameterizedbuilds.item.JenkinsResponse;
import com.kylenicholls.stash.parameterizedbuilds.item.JenkinsResponse.JenkinsMessage;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

public class Jenkins {

	private static final String PLUGIN_KEY = "com.kylenicholls.stash.parameterized-builds";
	private static final String JENKINS_SETTINGS = ".jenkinsSettings";
	private static final String JENKINS_USER = ".jenkinsUser.";
	private final PluginSettings pluginSettings;

	public Jenkins(PluginSettingsFactory factory) {
		this.pluginSettings = factory.createSettingsForKey(PLUGIN_KEY);
	}

	protected void setSettings(String url, String user, String token, boolean altUrl) {
		if (url != null && !url.isEmpty()) {
			String altUrlString = altUrl ? "true" : "false";
			pluginSettings
					.put(JENKINS_SETTINGS, url + ";" + user + ";" + token + ";" + altUrlString);
		} else {
			pluginSettings.remove(JENKINS_SETTINGS);
		}
	}

	protected void setUserSettings(@Nullable ApplicationUser user, String token) {
		if (user != null) {
			if (token != null && !token.isEmpty()) {
				pluginSettings.put(JENKINS_USER + user.getSlug(), token);
			} else {
				pluginSettings.remove(JENKINS_USER + user.getSlug());
			}
		}
	}

	@Nullable
	public Server getSettings() {
		Object settingObj = pluginSettings.get(JENKINS_SETTINGS);
		if (settingObj != null) {
			String[] serverProps = settingObj.toString().split(";");
			boolean altUrl = serverProps.length > 3 && serverProps[3].equals("true") ? true : false;
			return new Server(serverProps[0], serverProps[1], serverProps[2], altUrl);
		} else {
			return null;
		}
	}

	@Nullable
	public String getUserToken(@Nullable ApplicationUser user) {
		if (user != null && getUserSettings(user) != null) {
			return user.getSlug() + ":" + getUserSettings(user);
		}
		return null;
	}

	@Nullable
	protected String getUserSettings(@Nullable ApplicationUser user) {
		if (user != null) {
			Object settingObj = pluginSettings.get(JENKINS_USER + user.getSlug());
			if (settingObj != null) {
				return settingObj.toString();
			}
		}
		return null;
	}

	public JenkinsResponse triggerJob(Job job, String queryParams, String userToken) {
		Server server = getSettings();
		if (server == null) {
			return new JenkinsResponse.JenkinsMessage().error(true)
					.messageText("Jenkins settings are not setup").build();
		}

		String buildUrl = server.getBaseUrl() + job.buildUrl(server.getAltUrl(), queryParams, userToken);

		boolean prompt = false;
		if (userToken == null) {
			prompt = true;
			if (server.getUser() != null && !server.getUser().isEmpty()) {
				userToken = server.getUser() + ":" + server.getToken();
			}
		}
		return httpPost(buildUrl.replace(" ", "%20"), userToken, prompt);
	}

	private JenkinsResponse httpPost(String buildUrl, String token, boolean prompt) {
		JenkinsMessage jenkinsMessage = new JenkinsResponse.JenkinsMessage().prompt(prompt);
		try {
			URL url = new URL(buildUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			if (token != null && !token.isEmpty()) {
				byte[] authEncBytes = Base64.encodeBase64(token.getBytes());
				String authStringEnc = new String(authEncBytes);
				connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			}

			connection.setReadTimeout(45000);
			connection.setInstanceFollowRedirects(true);
			HttpURLConnection.setFollowRedirects(true);

			connection.connect();

			int status = connection.getResponseCode();
			if (status == 201) {
				return jenkinsMessage.messageText("Build triggered").build();
			} else if (status == 403) {
				return jenkinsMessage.error(true)
						.messageText("You do not have permissions to build this job").build();
			} else if (status == 404) {
				return jenkinsMessage.error(true).messageText("Job was not found").build();
			} else if (status == 500) {
				return jenkinsMessage.error(true)
						.messageText("Error triggering job, invalid build parameters").build();
			} else {
				return jenkinsMessage.error(true).messageText(connection.getResponseMessage())
						.build();
			}

		} catch (MalformedURLException e) {
			return jenkinsMessage.error(true).messageText("Malformed URL:" + e.getMessage())
					.build();
		} catch (IOException e) {
			return jenkinsMessage.error(true).messageText("IO exception occurred" + e.getMessage())
					.build();
		} catch (Exception e) {
			return jenkinsMessage.error(true).messageText("Something went wrong: " + e.getMessage())
					.build();
		}
	}
}
