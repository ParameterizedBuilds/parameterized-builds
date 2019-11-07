package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.google.common.collect.ImmutableMap;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

import java.util.List;
import java.util.Map;

public abstract class CIServer {

    List<Server> servers;
    Jenkins jenkins;
    JenkinsConnection jenkinsConn;

    String JENKINS_SETTINGS;
    String ADDITIONAL_JS;
    static final String SERVER = "server";
    static final String ERRORS = "errors";
    static final String TESTMESSAGE = "testMessage";

    public ImmutableMap<String, Object> renderMap(){
        return renderMap(ImmutableMap.of());
    }

    public abstract ImmutableMap<String, Object> renderMap(Map<String, Object> renderOptions);
}
