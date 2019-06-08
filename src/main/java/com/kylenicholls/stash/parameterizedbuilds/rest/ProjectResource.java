package com.kylenicholls.stash.parameterizedbuilds.rest;

import java.util.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.sun.jersey.spi.resource.Singleton;

@Path("/projects/{projectKey}")
@Singleton
public class ProjectResource extends RestResource implements ServerService {
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
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response getServers(@Context UriInfo ui){
        if (authContext.isAuthenticated()) {
            String projectKey = ui.getQueryParameters().getFirst("projectKey");

            List<Map<String, String>> servers = new ArrayList<>();

            Optional.ofNullable(jenkins.getJenkinsServer(projectKey))
                .map(x -> createServerMap(x, projectKey))
                .ifPresent(servers::add);

            return Response.ok(servers).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }
}
