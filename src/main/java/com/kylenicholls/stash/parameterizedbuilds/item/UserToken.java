package com.kylenicholls.stash.parameterizedbuilds.item;

public class UserToken {
	private String baseUrl;
	private String projectKey;
	private String projectName;
	private String userSlug;
	private String token;

	public UserToken(String baseUrl, String projectKey, String projectName, String userSlug, String token) {
		this.baseUrl = baseUrl;
		this.projectKey = projectKey;
		this.projectName = projectName;
		this.userSlug = userSlug;
		this.token = token;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getUserSlug() {
		return userSlug;
	}

	public String getToken() {
		return token;
	}
}
