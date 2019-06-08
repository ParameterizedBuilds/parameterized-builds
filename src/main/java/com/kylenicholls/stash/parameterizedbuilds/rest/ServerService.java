package com.kylenicholls.stash.parameterizedbuilds.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

    default Map<String, String> createServerMap(Server server, String projectKey){
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