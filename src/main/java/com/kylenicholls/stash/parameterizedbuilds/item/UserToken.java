package com.kylenicholls.stash.parameterizedbuilds.item;

import com.google.gson.JsonObject;

public class UserToken {
    private String baseUrl;
    private String alias;
    private String projectKey;
    private String projectName;
    private String userSlug;
    private String token;

    public UserToken(String baseUrl, String alias, String projectKey, String projectName, 
            String userSlug, String token) {
        this.baseUrl = baseUrl;
        this.alias = alias;
        this.projectKey = projectKey;
        this.projectName = projectName;
        this.userSlug = userSlug;
        this.token = token;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getAlias() {
        return alias;
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

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("url", this.baseUrl);
        jsonObject.addProperty("alias", this.alias);
        jsonObject.addProperty("project_key", this.projectKey);
        jsonObject.addProperty("project_name", this.projectName);
        jsonObject.addProperty("default_user", this.userSlug);
        jsonObject.addProperty("default_token", this.token);
        return jsonObject;
    }
}
