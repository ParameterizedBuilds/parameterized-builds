package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

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
	
	public void setSettings(String url, String user, String token){
		if (url != null && !url.isEmpty() 
				&& user != null && !user.isEmpty() 
				&& token != null && !token.isEmpty()) {
			pluginSettings.put(".jenkinsSettings", url + ";" + user + ";" + token);       
		} else {
			pluginSettings.remove(".jenkinsSettings");
		}
	}

	public void setUserSettings(String user, String token){
		if (user != null && !user.trim().isEmpty() 
				&& token != null && !token.isEmpty()) {
			pluginSettings.put(".jenkinsUser." + user, token);
		} else {
			pluginSettings.remove(".jenkinsUser." + user);
		}
	}

	public Server getSettings() {
		Object settingObj = pluginSettings.get(".jenkinsSettings");
		if (settingObj != null) {
			String[] serverProps = settingObj.toString().split(";");
			return new Server(serverProps[0], serverProps[1], serverProps[2]);
		} else {
			return null;
		}
	}

	public String getUserToken(String user) {
		if (getUserSettings(user) != null) {
			return user + ":" + getUserSettings(user);
		}
		return null;
	}
	
	public String getUserSettings(String user) {
		Object settingObj = pluginSettings.get(".jenkinsUser." + user);
		if (settingObj != null) {
			return settingObj.toString();
		} else {
			return null;
		}
	}

	public String[] triggerJob(Job job, String queryParams, String userToken){
		String buildUrl = "";
		Server server = getSettings();
		if (server == null) {
			return new String[]{"error", "Jenkins settings are not setup"};
		}
		
		String jobName = job.getJobName();
		
		System.out.println("queryParams:" + queryParams);
		String ciServer = server.getBaseUrl();
		
	    if (userToken == null && job.getToken() != null && !job.getToken().isEmpty()){
	    	if (queryParams.trim().isEmpty()){queryParams = "token=" + job.getToken();}
	    	else {queryParams += "&token=" + job.getToken();}
	    }

		System.out.println("queryParams2:" + queryParams);
	    if (queryParams.trim().isEmpty()){
			buildUrl = ciServer + "/job/" + jobName + "/build";
		} else if (queryParams.contains("token=") && queryParams.split("&").length < 2 ){
			buildUrl = ciServer + "/job/" + jobName + "/build?" + queryParams;
		} else {
			buildUrl = ciServer + "/job/" + jobName + "/buildWithParameters?" + queryParams;
		}
	    
		boolean prompt = false;
		if (userToken == null){
			prompt = true;
			userToken = server.getUser() + ":" + server.getToken();
		}
		System.out.println("buildUrl:" + buildUrl);
		return httpPost(buildUrl, userToken, prompt);
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
				connection.setRequestProperty("Authorization", "Basic "
						+ authStringEnc);
			}

			connection.setReadTimeout(5000);
			connection.setInstanceFollowRedirects(true);
			HttpURLConnection.setFollowRedirects(true);

			connection.connect();

			status = connection.getResponseCode();
			results[0] = Integer.toString(status);
			if (status == 201) {
				if (prompt){results[0] = "prompt";}
				results[1] = "201: Build triggered";
				return results;
			} else if (status == 401) {
				results[1] = "401: You do not have the required permissions";
				return results;
			} else if (status == 403) {
				results[1] = "403: Invalid build token";
				return results;
			} else if (status == 404) {
				results[1] = "404: Jenkins job does not exist";
				return results;
			} else if (status == 500) {
				results[1] = "500: Build request invalid; a build parameter may be incorrect";
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
