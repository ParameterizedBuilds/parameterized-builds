package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.google.common.collect.ImmutableMap;
import com.kylenicholls.stash.parameterizedbuilds.item.UserToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AccountServer extends CIServer {

    private static final String PROJECT_KEY_PREFIX = "projectKey-";
    private static final String USER_TOKEN_PREFIX = "jenkinsToken-";
    private static final String PROJECT_TOKENS_KEY = "projectTokens";
    private static final String USER_KEY = "user";

    private final transient ProjectService projectService;
    private ApplicationUser user;
    private  Map<String, String[]> parameters;

    public AccountServer(Jenkins jenkins, Map<String, String[]> parameters, ApplicationUser user,
                         ProjectService projectService){
        this.jenkins = jenkins;
        this.parameters = parameters;
        this.user = user;
        this.projectService = projectService;
        this.JENKINS_SETTINGS = "jenkins.user.settings";
        this.ADDITIONAL_JS = "jenkins-user-settings-form";
    }

    public Map<String, Object> postSettings(boolean clearSettings) {
        Set<String> parameterKeys = parameters.keySet();
        for (String key : parameterKeys) {
            if (key.startsWith(PROJECT_KEY_PREFIX)) {
                String projectKey = parameters.get(key)[0];
                String token = parameters.get(USER_TOKEN_PREFIX + projectKey)[0];
                jenkins.saveUserToken(user.getSlug(), projectKey, token);
            }
        }
        return null;
    }

    public ImmutableMap<String, Object> renderMap(Map<String, Object> renderOptions){
        List<UserToken> projectTokens = jenkins
                .getAllUserTokens(user, projectService.findAllKeys(), projectService);
        @SuppressWarnings("serial")
        Map<String, Object> baseMap = new HashMap<String, Object>() {{
            put(USER_KEY, user);
            put(PROJECT_TOKENS_KEY, projectTokens);
            put(ERRORS, "");
            putAll(renderOptions);
        }};
        return ImmutableMap.copyOf(baseMap);
    }
}
