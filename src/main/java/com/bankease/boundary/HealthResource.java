package com.bankease.boundary;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/health")
public class HealthResource {

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping() {
        String json = "{\"app\":\"bankease\",\"runtime\":\"payara\",\"status\":\"ok\"}";
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
}
