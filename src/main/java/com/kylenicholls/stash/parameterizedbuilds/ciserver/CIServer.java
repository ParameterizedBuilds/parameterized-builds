package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.google.common.collect.ImmutableMap;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

import java.util.Map;

public abstract class CIServer {

    Server server;
    Jenkins jenkins;

    String JENKINS_SETTINGS;
    static final String SERVER = "server";
    static final String ERRORS = "errors";

    public abstract Map<String, Object> postSettings(boolean clearSettings);

    public  ImmutableMap<String, Object> renderMap(){
        return renderMap("");
    }

    public abstract ImmutableMap<String, Object> renderMap(String error);

    public String testConnection(){
        return jenkins.testConnection(server);
    }
}
