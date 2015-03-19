package com.kylenicholls.stash.parameterizedbuilds.item;

public class Server {
	private String baseUrl;
    private String user;
    private String token;

	public Server(String baseUrl, String user, String token) {
    	this.baseUrl = baseUrl;
    	this.user = user;
    	this.token = token;
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
}
