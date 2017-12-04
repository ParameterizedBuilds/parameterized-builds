package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.google.common.collect.ImmutableMap;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

import java.util.Map;

public class ProjectServer extends CIServer{

    private static final String PROJECT_KEY = "projectKey";

    private String projectKey;

    public ProjectServer(Jenkins jenkins, Server server, String projectKey){
        this.jenkins = jenkins;
        this.server = server;
        this.projectKey = projectKey;
        this.JENKINS_SETTINGS =  "jenkins.admin.settingsProjectAdmin";
    }

    public Map<String, Object> postSettings(boolean clearSettings){
        if (clearSettings) {
            jenkins.saveJenkinsServer(null, projectKey);
        } else if (server.getBaseUrl().isEmpty()) {
            renderMap("Base URL required");
        } else {
            jenkins.saveJenkinsServer(server, projectKey);
        }
        return null;
    }

    public  ImmutableMap<String, Object> renderMap(String error){
        return ImmutableMap.of(SERVER, server != null ? server : "", PROJECT_KEY, projectKey, ERRORS, error);
    }
}
