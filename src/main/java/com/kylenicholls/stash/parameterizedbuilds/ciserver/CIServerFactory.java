package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.google.common.collect.Lists;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

import java.util.List;
import java.util.Map;

public class CIServerFactory {

    public static CIServer getServer(String pathInfo, Jenkins jenkins,
                                     Map<String, String[]> parameters,
                                     ApplicationUser user, ProjectService projectService){
        if (pathInfo.contains("/jenkins/account")){
            return new AccountServer(jenkins, user, projectService);
        } else if (pathInfo.contains("/jenkins/project/")) {
            Server server = getServerFromMap(parameters);
            List<Server> servers = Lists.newArrayList(server);
            String projectKey = pathInfo.replaceAll(".*/jenkins/project/", "")
                    .split("/")[0];
            return new ProjectServer(jenkins, servers, projectKey);
        } else {
            Server server = getServerFromMap(parameters);
            List<Server> servers = Lists.newArrayList(server);
            return new GlobalServer(jenkins, servers);
        }
    }

    public static CIServer getServer(String pathInfo, Jenkins jenkins,
                                     ApplicationUser user, ProjectService projectService){
        if (pathInfo.contains("/jenkins/account")){
            return new AccountServer(jenkins, user, projectService);
        } else if (pathInfo.contains("/jenkins/project/")) {
            String projectKey = pathInfo.replaceAll(".*/jenkins/project/", "")
                    .split("/")[0];
            List<Server> servers = jenkins.getJenkinsServers(projectKey);
            return new ProjectServer(jenkins, servers, projectKey);
        } else {
            List<Server> servers =jenkins.getJenkinsServers(null);
            return new GlobalServer(jenkins, servers);
        }
    }

    private static Server getServerFromMap(Map<String, String[]> parameters) {
        boolean jenkinsAltUrl = parameters.get("jenkinsAltUrl") != null
                && "on".equals(parameters.get("jenkinsAltUrl")[0]) ? true : false;
        boolean jenkinsCSRF = parameters.get("jenkinsCSRF") != null
                && "on".equals(parameters.get("jenkinsCSRF")[0]) ? true : false;
        String jenkinsAlias = parameters.getOrDefault("jenkinsAlias", new String[]{""})[0];
        return new Server(parameters.get("jenkinsUrl")[0], jenkinsAlias, 
                parameters.get("jenkinsUser")[0], parameters.get("jenkinsToken")[0], jenkinsAltUrl, 
                jenkinsCSRF);
    }
}
