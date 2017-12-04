package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.google.common.collect.ImmutableMap;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

import java.util.Map;

public class GlobalServer extends CIServer{

    public GlobalServer(Jenkins jenkins, Server server){
        this.jenkins = jenkins;
        this.server = server;
        this.JENKINS_SETTINGS = "jenkins.admin.settings";
    }

    public Map<String, Object> postSettings(boolean clearSettings){
        if (clearSettings) {
            jenkins.saveJenkinsServer(null);
        } else if (server.getBaseUrl().isEmpty()) {
            return renderMap("Base URL required");
        } else {
            jenkins.saveJenkinsServer(server);
        }
        return null;
    }

    public  ImmutableMap<String, Object> renderMap(String error){
        return ImmutableMap.of(SERVER, server != null ? server : "", ERRORS, error);
    }
}
