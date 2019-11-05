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
        this.jenkinsConn = new JenkinsConnection(jenkins);
        this.server = server;
        this.projectKey = projectKey;
        this.JENKINS_SETTINGS = "jenkins.admin.settingsProjectAdmin";
        this.ADDITIONAL_JS = "jenkins-settings-form";
    }

    public ImmutableMap<String, Object> renderMap(Map<String, Object> renderOptions){
        Object server = this.server != null ? this.server: "";
        @SuppressWarnings("serial")
        Map<String, Object> baseMap = new HashMap<String, Object>() {{
            put(SERVER, server);
            put(PROJECT_KEY, projectKey);
            putAll(renderOptions);
        }};
        return ImmutableMap.copyOf(baseMap);
    }
}
