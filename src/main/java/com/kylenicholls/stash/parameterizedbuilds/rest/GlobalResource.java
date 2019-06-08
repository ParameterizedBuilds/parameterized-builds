package com.kylenicholls.stash.parameterizedbuilds.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
            List<Map<String, String>> servers = new ArrayList<>();
            Optional.ofNullable(jenkins.getJenkinsServer(null))
                .map(x -> createServerMap(x, null)).ifPresent(servers::add);

            return Response.ok(servers).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }
}