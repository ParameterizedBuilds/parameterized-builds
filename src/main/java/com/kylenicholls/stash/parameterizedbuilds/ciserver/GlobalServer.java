package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.google.common.collect.ImmutableMap;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalServer extends CIServer{

    public GlobalServer(Jenkins jenkins, List<Server> servers){
        this.jenkins = jenkins;
        this.jenkinsConn = new JenkinsConnection(jenkins);
        this.servers = servers;
        this.JENKINS_SETTINGS = "jenkins.admin.settings";
        this.ADDITIONAL_JS = "jenkins-settings-form";
    }

    public ImmutableMap<String, Object> renderMap(Map<String, Object> renderOptions){
        @SuppressWarnings("serial")
        Map<String, Object> baseMap = new HashMap<String, Object>() {{
            put(SERVER,servers);
            putAll(renderOptions);
        }};
        return ImmutableMap.copyOf(baseMap);
    }
}
