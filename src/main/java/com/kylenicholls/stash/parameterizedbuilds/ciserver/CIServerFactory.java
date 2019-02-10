package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

import java.util.Map;

public class CIServerFactory {

    public static CIServer getServer(String pathInfo, Jenkins jenkins, Map<String, String[]> parameters,
                                     ApplicationUser user, ProjectService projectService){
        if (pathInfo.contains("/jenkins/account")){
            return new AccountServer(jenkins, parameters, user, projectService);
        } else if (pathInfo.contains("/jenkins/project/")) {
            Server server = getServerFromMap(parameters);
            String projectKey = pathInfo.replaceAll(".*/jenkins/project/", "")
                    .split("/")[0];
            return new ProjectServer(jenkins, server, projectKey);
        } else {
            Server server = getServerFromMap(parameters);
            return new GlobalServer(jenkins, server);
        }
    }

    public static CIServer getServer(String pathInfo, Jenkins jenkins,
                                     ApplicationUser user, ProjectService projectService){
        if (pathInfo.contains("/jenkins/account")){
            return new AccountServer(jenkins, null, user, projectService);
        } else if (pathInfo.contains("/jenkins/project/")) {
            String projectKey = pathInfo.replaceAll(".*/jenkins/project/", "")
                    .split("/")[0];
            Server server = jenkins.getJenkinsServer(projectKey);
            return new ProjectServer(jenkins, server, projectKey);
        } else {
            Server server =jenkins.getJenkinsServer(null);
            return new GlobalServer(jenkins, server);
        }
    }

    private static Server getServerFromMap(Map<String, String[]> parameters) {
        boolean jenkinsAltUrl = parameters.get("jenkinsAltUrl") != null
                && "on".equals(parameters.get("jenkinsAltUrl")[0]) ? true : false;
        boolean jenkinsCSRF = parameters.get("jenkinsCSRF") != null
                && "on".equals(parameters.get("jenkinsCSRF")[0]) ? true : false;
        String jenkinsAlias = parameters.getOrDefault("jenkinsAlias", new String[]{""})[0];
        return new Server(parameters.get("jenkinsUrl")[0], jenkinsAlias, parameters.get("jenkinsUser")[0],
                parameters.get("jenkinsToken")[0], jenkinsAltUrl, jenkinsCSRF);
    }
}
