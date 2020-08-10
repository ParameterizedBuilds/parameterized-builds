package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.google.common.collect.ImmutableMap;
import com.kylenicholls.stash.parameterizedbuilds.item.UserToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;

public class AccountServer extends CIServer {

    private static final String PROJECT_TOKENS_KEY = "projectTokens";
    private static final String USER_KEY = "user";

    private final transient ProjectService projectService;
    private ApplicationUser user;
    private Jenkins jenkins;

    public AccountServer(Jenkins jenkins, ApplicationUser user, ProjectService projectService){
        this.jenkins = jenkins;
        this.user = user;
        this.projectService = projectService;
        this.JENKINS_SETTINGS = "jenkins.user.settings";
        this.ADDITIONAL_JS = "jenkins-user-settings-form";
    }

    public ImmutableMap<String, Object> renderMap(Map<String, Object> renderOptions){
        List<UserToken> projectTokens = jenkins
                .getAllUserTokens(user, projectService.findAllKeys(), projectService);

        JsonArray tokenArray = new JsonArray();
        projectTokens.stream()
                .map(UserToken::toJson)
                .forEach(tokenArray::add);
        
        @SuppressWarnings("serial")
        Map<String, Object> baseMap = new HashMap<String, Object>() {{
            put(USER_KEY, user);
            put(PROJECT_TOKENS_KEY, tokenArray.toString());
            putAll(renderOptions);
        }};
        return ImmutableMap.copyOf(baseMap);
    }
}
