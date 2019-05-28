package com.kylenicholls.stash.parameterizedbuilds.rest;

import java.util.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.rest.RestResource;
import com.atlassian.bitbucket.rest.util.RestUtils;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import com.sun.jersey.spi.resource.Singleton;

@Path("/{projectKey}")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ RestUtils.APPLICATION_JSON_UTF8 })
@Singleton
public class ProjectResource extends RestResource {
    private Jenkins jenkins;
    private final AuthenticationContext authContext;

    public ProjectResource(I18nService i18nService, Jenkins jenkins,
            AuthenticationContext authContext) {
        super(i18nService);
        this.jenkins = jenkins;
        this.authContext = authContext;
    }

    @GET
    @Path("/servers")
    public Response getJenkinsServer(@PathParam("projectKey") String projectKey){
        if (authContext.isAuthenticated()) {
            List<Map<String, String>> servers = new ArrayList<>();

            Optional.ofNullable(jenkins.getJenkinsServer(projectKey))
                .map(x -> createServerMap(x, projectKey)).ifPresent(servers::add);
            Optional.ofNullable(jenkins.getJenkinsServer(null))
                    .map(x -> createServerMap(x, null)).ifPresent(servers::add);

            return Response.ok(servers).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    private Map<String, String> createServerMap(Server server, String projectKey){
        Map<String, String> serverMap = new HashMap<>();
        serverMap.put("url", server.getBaseUrl());
        serverMap.put("alias", server.getAlias());
        serverMap.put("scope", projectKey == null ? "global": "project");
        serverMap.put("project", projectKey);
        serverMap.put("default_user", server.getUser());
        serverMap.put("root_token_enabled", 
                      Boolean.toString(server.getAltUrl()));
        serverMap.put("csrf_enabled", 
                      Boolean.toString(server.getCsrfEnabled()));
        return serverMap;
    }
}
