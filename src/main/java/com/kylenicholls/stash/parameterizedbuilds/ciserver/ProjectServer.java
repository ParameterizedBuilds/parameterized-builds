package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.google.common.collect.ImmutableMap;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

            renderMap(Stream.of("Base URL required")
                    .collect(Collectors.toMap(x -> ERRORS, Function.identity())));
        } else {
            jenkins.saveJenkinsServer(server, projectKey);
        }
        return null;
    }

    public ImmutableMap<String, Object> renderMap(Map<String, Object> renderOptions){
        Object server = this.server != null ? this.server: "";
        Map<String, Object> baseMap = new HashMap<>();
        baseMap.put(SERVER, server);
        baseMap.put(PROJECT_KEY, projectKey);
        baseMap.put(ERRORS, "");
        baseMap.put(TESTMESSAGE, "");
        baseMap.putAll(renderOptions);
        return ImmutableMap.copyOf(baseMap);
    }
}
