package com.kylenicholls.stash.parameterizedbuilds.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.atlassian.bitbucket.rest.util.RestUtils;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

public interface ServerService {

    @GET
    @Path("/servers")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response getServers(@Context UriInfo ui);

    @PUT
    @Path("/servers/{serverAlias}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response addServer(@Context UriInfo ui, Server server);

    default Map<String, Object> createServerMap(Server server, String projectKey){
        Map<String, Object> serverMap = new HashMap<>();
        serverMap.put("url", server.getBaseUrl());
        serverMap.put("alias", server.getAlias());
        serverMap.put("scope", projectKey == null ? "global": "project");
        serverMap.put("project", projectKey);
        serverMap.put("default_user", server.getUser());
        serverMap.put("root_token_enabled", server.getAltUrl());
        serverMap.put("csrf_enabled", server.getCsrfEnabled());
        return serverMap;
    }

    default Server mapToServer(Map<String, Object> serverMap){
        return new Server(serverMap);
    }

}