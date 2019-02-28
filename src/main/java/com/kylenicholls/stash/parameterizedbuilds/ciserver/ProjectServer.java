package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.google.common.collect.ImmutableMap;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

import java.util.HashMap;
import java.util.Map;

public class ProjectServer extends CIServer{

    static final String PROJECT_KEY = "projectKey";

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
            return renderMap(ImmutableMap.of(ERRORS, "Base URL required"));
        } else {
            jenkins.saveJenkinsServer(server, projectKey);
        }
        return null;
    }

    public ImmutableMap<String, Object> renderMap(Map<String, Object> renderOptions){
        Object server = this.server != null ? this.server: "";
        @SuppressWarnings("serial")
        Map<String, Object> baseMap = new HashMap<String, Object>() {{
            put(SERVER, server);
            put(PROJECT_KEY, projectKey);
            put(ERRORS, "");
            put(TESTMESSAGE, "");
            putAll(renderOptions);
        }};
        return ImmutableMap.copyOf(baseMap);
    }
}
