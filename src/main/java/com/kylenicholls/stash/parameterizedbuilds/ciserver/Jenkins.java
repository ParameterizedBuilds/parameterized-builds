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
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

public class Jenkins {

	private static final String PLUGIN_KEY = "com.kylenicholls.stash.parameterized-builds";
	private final PluginSettings pluginSettings;

	public Jenkins(PluginSettingsFactory factory) {
		this.pluginSettings = factory.createSettingsForKey(PLUGIN_KEY);
	}

	public void setSettings(String url, String user, String token, boolean altUrl) {
		if (url != null && !url.isEmpty()) {
			String altUrlString = altUrl ? "true" : "false";
			pluginSettings
					.put(".jenkinsSettings", url + ";" + user + ";" + token + ";" + altUrlString);
		} else {
			pluginSettings.remove(".jenkinsSettings");
		}
	}

	public void setUserSettings(@Nullable ApplicationUser user, String token) {
		if (user != null) {
			if (token != null && !token.isEmpty()) {
				pluginSettings.put(".jenkinsUser." + user.getSlug(), token);
			} else {
				pluginSettings.remove(".jenkinsUser." + user.getSlug());
			}
		}
	}

	@Nullable
	public Server getSettings() {
		Object settingObj = pluginSettings.get(".jenkinsSettings");
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
	public String getUserSettings(@Nullable ApplicationUser user) {
		if (user != null) {
			Object settingObj = pluginSettings.get(".jenkinsUser." + user.getSlug());
			if (settingObj != null) {
				return settingObj.toString();
			}
		}
		return null;
	}

	public String[] triggerJob(Job job, String queryParams, String userToken) {
		String buildUrl = "";
		Server server = getSettings();
		if (server == null) {
			return new String[] { "error", "Jenkins settings are not setup" };
		}

		String jobName = job.getJobName();

		String ciServer = server.getBaseUrl();
		boolean altUrl = server.getAltUrl();

		if (userToken == null && job.getToken() != null && !job.getToken().isEmpty()) {
			if (queryParams.trim().isEmpty()) {
				queryParams = "token=" + job.getToken();
			} else {
				queryParams += "&token=" + job.getToken();
			}
		}

		if (queryParams.trim().isEmpty()) {
			buildUrl = ciServer + "/job/" + jobName + "/build";
		} else if (queryParams.contains("token=") && queryParams.split("&").length < 2) {
			if (altUrl && (userToken == null || !userToken.equals(""))) {
				buildUrl = ciServer + "/buildByToken/build?job=" + jobName + "&" + queryParams;
			} else {
				buildUrl = ciServer + "/job/" + jobName + "/build?" + queryParams;
			}
		} else {
			if (altUrl && (userToken == null || userToken.equals(""))) {
				buildUrl = ciServer + "/buildByToken/buildWithParameters?job=" + jobName + "&"
						+ queryParams;
			} else {
				buildUrl = ciServer + "/job/" + jobName + "/buildWithParameters?" + queryParams;
			}
		}

		boolean prompt = false;
		if (userToken == null) {
			prompt = true;
			if (server.getUser() != null && !server.getUser().equals("")) {
				userToken = server.getUser() + ":" + server.getToken();
			}
		}
		return httpPost(buildUrl.replace(" ", "%20"), userToken, prompt);
	}

	public String[] httpPost(String buildUrl, String token, boolean prompt) {
		String[] results = new String[2];
		int status = 0;
		// Trigger build using build URL from hook setting
		try {
			URL url = new URL(buildUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			if (token != null && !token.equals("")) {
				byte[] authEncBytes = Base64.encodeBase64(token.getBytes());
				String authStringEnc = new String(authEncBytes);
				connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			}

			connection.setReadTimeout(30000);
			connection.setInstanceFollowRedirects(true);
			HttpURLConnection.setFollowRedirects(true);

			connection.connect();

			status = connection.getResponseCode();
			results[0] = Integer.toString(status);
			if (status == 201) {
				results[1] = "201: Build triggered";
				return results;
			} else {
				results[1] = status + ": " + connection.getResponseMessage();
				return results;
			}

			// log.debug("HTTP response:\n" + body.toString());
		} catch (MalformedURLException e) {
			// log.error("Malformed URL:" + e);
		} catch (IOException e) {
			// log.error("Some IO exception occurred", e);
		} catch (Exception e) {
			// log.error("Something else went wrong: ", e);
		}

		results[0] = "error";
		results[1] = status + ": unknown error";
		return results;
	}
}
