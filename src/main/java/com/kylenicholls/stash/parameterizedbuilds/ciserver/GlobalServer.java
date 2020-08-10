package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class GlobalServer extends CIServer{

    public GlobalServer(){
        this.JENKINS_SETTINGS = "jenkins.admin.settings";
        this.ADDITIONAL_JS = "jenkins-settings-form";
    }

    public ImmutableMap<String, Object> renderMap(Map<String, Object> renderOptions){
        @SuppressWarnings("serial")
        Map<String, Object> baseMap = new HashMap<String, Object>() {{
            putAll(renderOptions);
        }};
        return ImmutableMap.copyOf(baseMap);
    }
}
