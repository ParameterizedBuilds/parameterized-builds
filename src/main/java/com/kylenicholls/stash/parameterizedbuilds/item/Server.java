package com.kylenicholls.stash.parameterizedbuilds.item;

import java.util.HashMap;
import java.util.Map;

public class Server {
	private String baseUrl;
	private String user;
	private String token;
	private boolean altUrl;

	public Server(String baseUrl, String user, String token, boolean altUrl) {
		this.baseUrl = baseUrl;
		this.user = user;
		this.token = token;
		this.altUrl = altUrl;
	}

	public Server(Map<String, Object> map) {
		this.baseUrl = (String) map.get("baseUrl");
		this.user = (String) map.get("user");
		this.token = (String) map.get("token");
		this.altUrl = Boolean.parseBoolean(map.get("altUrl").toString());
	}

	public String getBaseUrl() {
		if (baseUrl.endsWith("/")) {
			return baseUrl.substring(0, baseUrl.length() - 1);
		}
		return baseUrl;
	}

	public String getUser() {
		return user;
	}

	public String getToken() {
		return token;
	}

	public boolean getAltUrl() {
		return altUrl;
	}

	public Map<String, Object> asMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("baseUrl", baseUrl);
		map.put("user", user);
		map.put("token", token);
		map.put("altUrl", altUrl);
		return map;
	}

	public String getJoinedToken() {
		return user + ":" + token;
	}
}
