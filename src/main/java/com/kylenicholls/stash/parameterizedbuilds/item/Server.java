package com.kylenicholls.stash.parameterizedbuilds.item;

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
}
