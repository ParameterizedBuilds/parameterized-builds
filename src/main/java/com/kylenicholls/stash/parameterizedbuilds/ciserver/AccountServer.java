package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.google.common.collect.ImmutableMap;
import com.kylenicholls.stash.parameterizedbuilds.item.UserToken;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AccountServer extends CIServer {

    private static final String PROJECT_KEY_PREFIX = "projectKey-";
    private static final String USER_TOKEN_PREFIX = "jenkinsToken-";

    private final transient ProjectService projectService;
    private ApplicationUser user;
    private  Map<String, String[]> parameters;

    public AccountServer(Jenkins jenkins, Map<String, String[]> parameters, ApplicationUser user,
                         ProjectService projectService){
        this.jenkins = jenkins;
        this.parameters = parameters;
        this.user = user;
        this.projectService = projectService;
        this.JENKINS_SETTINGS =  "jenkins.user.settings";
    }

    public Map<String, Object> postSettings(boolean _) {
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

    public  ImmutableMap<String, Object> renderMap(String error){
        List<UserToken> projectTokens = jenkins
                .getAllUserTokens(user, projectService.findAllKeys(), projectService);
        return ImmutableMap.of("user", user, "projectTokens", projectTokens, ERRORS, error);
    }
}
