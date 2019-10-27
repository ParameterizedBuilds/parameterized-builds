package com.kylenicholls.stash.parameterizedbuilds.rest;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.atlassian.bitbucket.rest.util.RestUtils;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

import org.apache.http.client.utils.URIBuilder;

public interface ServerService {

    @GET
    @Path("/servers")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response getServers(@Context UriInfo ui);

    @POST
    @Path("/servers/validate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response validate(@Context UriInfo ui, Server server);

    @PUT
    @Path("/servers/{serverAlias}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response addServer(@Context UriInfo ui, Server server);

    @DELETE
    @Path("/servers/{serverAlias}")
    public Response removeServer(@Context UriInfo ui);

    @PUT
    @Path("/servers/{serverAlias}/userToken")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response addUserToken(@Context UriInfo ui, String token);

    @DELETE
    @Path("/servers/{serverAlias}/userToken")
    public Response removeUserToken(@Context UriInfo ui);

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

    default List<String> sanitizeServerInput(Server server){
        List<String> errors = new ArrayList<>(2);
        if (server.getBaseUrl() == null || server.getBaseUrl().isEmpty()){
            errors.add("Base Url required.");
        } else {
            URIBuilder builder;
            try {
                builder = new URIBuilder(server.getBaseUrl());
                if (builder.getHost() == null){
                    errors.add("Invalide Base Url.");
                }
            } catch (URISyntaxException e) {
                errors.add("Invalide Base Url.");
            }
        }

        if (server.getAlias() == null || server.getAlias().isEmpty()){
            errors.add("Alias required.");
        }

        return errors;
    }

    default Server mapToServer(Map<String, Object> serverMap){
        return new Server(serverMap);
    }

    default String getCurrentDefaultToken(Server oldServer, Server newServer){
        // if the new server didn't edit the token attribute and the server
        // credentials should be the same, save the old token
        if (shouldUseOldToken(oldServer, newServer)) {
            return oldServer.getToken();
        } else if (newServer.getToken() == null) {
            return "";
        } else {
            return newServer.getToken();
        }
    }

    default boolean shouldUseOldToken(Server oldServer, Server newServer){
        return
            oldServer != null &&
            newServer.getToken() == null &&
            oldServer.getBaseUrl().equals(newServer.getBaseUrl()) &&
            oldServer.getUser().equals(newServer.getUser());
    }

}