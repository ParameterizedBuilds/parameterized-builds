package com.kylenicholls.stash.parameterizedbuilds.rest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.rest.RestResource;
import com.atlassian.bitbucket.rest.util.RestUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.JenkinsConnection;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import com.sun.jersey.spi.resource.Singleton;


@Path("/global")
@Singleton
public class GlobalResource extends RestResource implements ServerService{

    private Jenkins jenkins;
    private final AuthenticationContext authContext;

    public GlobalResource(I18nService i18nService, Jenkins jenkins,
            AuthenticationContext authContext) {
        super(i18nService);
        this.jenkins = jenkins;
        this.authContext = authContext;
    }

    @Override
    @GET
    @Path("/servers")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response getServers(@Context UriInfo ui){
        if (authContext.isAuthenticated()) {
            List<Map<String, Object>> servers = jenkins.getJenkinsServers(null).stream()
                    .map(x -> createServerMap(x, null))
                    .collect(Collectors.toList());

            return Response.ok(servers).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @POST
    @Path("/servers/validate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response validate(@Context UriInfo ui, Server server){
        if (authContext.isAuthenticated()) {
            Server oldServer = jenkins.getJenkinsServer(null, server.getAlias());
            server.setToken(getCurrentDefaultToken(oldServer, server));

            JenkinsConnection jenkinsConn = new JenkinsConnection(jenkins);
            String message = jenkinsConn.testConnection(server);

            if(message.equals("Connection successful")){
                return Response.ok(message).build();
            }

            return Response.status(400).entity(message).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @PUT
    @Path("/servers/{serverAlias}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response addServer(@Context UriInfo ui, Server server, 
                              @PathParam("id") String serverAlias){
        if (authContext.isAuthenticated()){
            List<String> errors = sanitizeServerInput(server);
            if (!errors.isEmpty()) {
                JsonArray errorMessages = new JsonArray();
                errors.forEach(error -> errorMessages.add(new JsonPrimitive(error)));
                JsonObject response = new JsonObject();
                response.add("errors", errorMessages);

                return Response.status(422).entity(response.toString()).build();
            }

            Server oldServer = jenkins.getJenkinsServer(null, serverAlias);
            server.setToken(getCurrentDefaultToken(oldServer, server));

            int returnStatus = oldServer == null ? 201 : 200;
            jenkins.saveJenkinsServer(server, null);
            return Response.status(returnStatus).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @DELETE
    @Path("/servers/{serverAlias}")
    public Response removeServer(@Context UriInfo ui){
        if (authContext.isAuthenticated()) {
            jenkins.saveJenkinsServer(null, null);
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @PUT
    @Path("/servers/{serverAlias}/userToken")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response addUserToken(@Context UriInfo ui, ServerService.Token token){
        if (authContext.isAuthenticated()) {
            String user = authContext.getCurrentUser().getSlug();
            jenkins.saveUserToken(user, "", token.getToken());
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @DELETE
    @Path("/servers/{serverAlias}/userToken")
    public Response removeUserToken(@Context UriInfo ui){
        if (authContext.isAuthenticated()) {
            String user = authContext.getCurrentUser().getSlug();
            jenkins.saveUserToken(user, "", "");
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }
}