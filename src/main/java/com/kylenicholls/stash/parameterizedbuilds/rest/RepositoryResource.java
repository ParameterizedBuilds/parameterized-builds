package com.kylenicholls.stash.parameterizedbuilds.rest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.bitbucket.ServiceException;
import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.rest.RestResource;
import com.atlassian.bitbucket.rest.util.RestUtils;
import com.atlassian.bitbucket.setting.Settings;
import com.google.common.collect.Lists;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.sun.jersey.spi.resource.Singleton;

// base path currently includes jobs to prevent duplicate path with BuildResource
@Path("/projects/{projectKey}/repos/{repositorySlug}/jobs")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ RestUtils.APPLICATION_JSON_UTF8 })
@Singleton
public class RepositoryResource extends RestResource {
    private final AuthenticationContext authContext;
    private SettingsService settingsService;

    public RepositoryResource(I18nService i18nService, SettingsService settingsService,
            AuthenticationContext authContext) {
        super(i18nService);
        this.settingsService = settingsService;
        this.authContext = authContext;
    }

    @GET
    @Path("/")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response getJobs(@Context final Repository repository){
        if (authContext.isAuthenticated()) {

            List<Job> jobs = settingsService.getJobs(repository);
            List<Map<String, Object>> data = jobs.stream()
                .map(Job::asRestMap)
                .collect(Collectors.toList());
            return Response.ok(data).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @GET
    @Path("/{jobId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response getJob(@Context final Repository repository,  @PathParam("jobId") int jobId){
        if (authContext.isAuthenticated()) {

            List<Job> jobs = settingsService.getJobs(repository);
            if (jobs.isEmpty()) {
                return Response.ok(Lists.newArrayList()).build();
            }

            Optional<Map<String, Object>> data = jobs.stream()
                .filter(job -> job.getJobId() == jobId)
                .map(Job::asRestMap)
                .findFirst();
            return data.map(job -> Response.ok(job).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).entity("Job not found").build());
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @PUT
    @Path("/{jobId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response addJob(@Context final Repository repository,  @PathParam("jobId") int jobId,
            Job job
    ){
        if (authContext.isAuthenticated()) {

            Settings settings = settingsService.getSettings(repository);
            if (settings == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            Job previousJob = settingsService.settingsToJob(jobId, settings);

            Settings newSettings = settingsService.addJobToSettings(settings, job).build();
            try{
                settingsService.putSettings(repository, newSettings);
            } catch (ServiceException e){
                return Response.status(422).entity(e.getMessage()).build();
            }
            return Response.status(previousJob != null ? 200 : 201).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @DELETE
    @Path("/{jobId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ RestUtils.APPLICATION_JSON_UTF8 })
    public Response deleteJob(@Context final Repository repository, @PathParam("jobId") int jobId){
        if (authContext.isAuthenticated()) {

            List<Job> jobs = settingsService.getJobs(repository);
            if (jobs.stream().noneMatch(job -> job.getJobId() == jobId)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            Settings newSettings = jobs.stream()
                    .filter(job -> job.getJobId() != jobId)
                    .reduce(settingsService.newSettings(repository),
                            settingsService::addJobToSettings,
                            (settings1, settings2) -> settings1.addAll(settings2.build()))
                    .build();

            try{
                settingsService.putSettings(repository, newSettings);
            } catch (ServiceException e){
                return Response.status(422).entity(e.getMessage()).build();
            }
            return Response.status(204).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }
}