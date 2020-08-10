package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.user.ApplicationUser;

public class CIServerFactory {

    public static CIServer getServer(String pathInfo, Jenkins jenkins,
                                     ApplicationUser user, ProjectService projectService){
        if (pathInfo.contains("/jenkins/account")){
            return new AccountServer(jenkins, user, projectService);
        } else if (pathInfo.contains("/jenkins/project/")) {
            String projectKey = pathInfo.replaceAll(".*/jenkins/project/", "")
                    .split("/")[0];
            return new ProjectServer(projectKey);
        } else {
            return new GlobalServer();
        }
    }
}
