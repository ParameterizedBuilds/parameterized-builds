package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public abstract class CIServer {

    String JENKINS_SETTINGS;
    String ADDITIONAL_JS;

    public ImmutableMap<String, Object> renderMap(){
        return renderMap(ImmutableMap.of());
    }

    public abstract ImmutableMap<String, Object> renderMap(Map<String, Object> renderOptions);
}
