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
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean getAltUrl() {
		return altUrl;
	}

	public void setAltUrl(boolean altUrl) {
		this.altUrl = altUrl;
	}
}
