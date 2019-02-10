package com.kylenicholls.stash.parameterizedbuilds.item;

import java.util.HashMap;
import java.util.Map;

public class Server {
	private String baseUrl;
	private String alias;
	private String user;
	private String token;
	private boolean altUrl;
	private boolean csrfEnabled;

	public Server(String baseUrl, String alias, String user, String token, boolean altUrl, boolean csrfEnabled) {
		this.baseUrl = baseUrl;
		this.alias = alias;
		this.user = user;
		this.token = token;
		this.altUrl = altUrl;
		this.csrfEnabled = csrfEnabled;
	}

	public Server(Map<String, Object> map) {
		this.baseUrl = (String) map.get("baseUrl");
		this.alias = (String) map.get("alias");
		this.user = (String) map.get("user");
		this.token = (String) map.get("token");
		this.altUrl = Boolean.parseBoolean(map.get("altUrl").toString());
		this.csrfEnabled = Boolean.parseBoolean(map.getOrDefault("csrfEnabled", "true").toString());
	}

	public String getBaseUrl() {
		if (baseUrl.endsWith("/")) {
			return baseUrl.substring(0, baseUrl.length() - 1);
		}
		return baseUrl;
	}

	public String getAlias() {
		return alias;
	}

	public String getUser() {
		return user;
	}

	public String getToken() {
		return token;
	}

	public boolean getCsrfEnabled() {
		return csrfEnabled;
	}

	public boolean getAltUrl() {
		return altUrl;
	}

	public Map<String, Object> asMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("baseUrl", baseUrl);
		map.put("alias", alias);
		map.put("user", user);
		map.put("token", token);
		map.put("altUrl", altUrl);
		map.put("csrfEnabled", csrfEnabled);
		return map;
	}

	public String getJoinedToken() {
		return user + ":" + token;
	}
}
