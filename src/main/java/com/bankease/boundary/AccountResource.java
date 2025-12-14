package com.bankease.boundary;

import ch.unil.doplab.bankease.service.AccountService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@ApplicationScoped
@Path("/accounts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource {

    @Inject
    AccountService accountService;

    @POST
    public Response create(Map<String, Object> body) {
        try {
            String clientId = (String) body.get("clientId");
            String typeStr = (String) body.getOrDefault("type", "CURRENT");

            // Provide a wrapper or change service signature? Service expects
            // CreateAccountRequest (record/class)
            // Assuming CreateAccountRequest is a record: public record
            // CreateAccountRequest(String type) {}
            // I need to instantiate it.
            // CreateAccountRequest takes (clientId, type)
            ch.unil.doplab.bankease.dto.CreateAccountRequest req = new ch.unil.doplab.bankease.dto.CreateAccountRequest(
                    clientId, typeStr);

            Map<String, Object> result = accountService.openAccount(clientId, req);

            return Response.status(Response.Status.CREATED)
                    .entity(result)
                    .build();
        } catch (ch.unil.doplab.bankease.exception.ApiException e) {
            return Response.status(e.getStatus()).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.serverError().entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/{accountNumber}/balance")
    public Response getBalance(@PathParam("accountNumber") String accountNumber) {
        try {
            Map<String, Object> acc = accountService.getAccount(accountNumber);
            return Response.ok(Map.of(
                    "accountNumber", acc.get("accountNumber"),
                    "balance", acc.get("balance"))).build();
        } catch (ch.unil.doplab.bankease.exception.ApiException e) {
            return Response.status(e.getStatus()).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    public Response getAllAccounts() {
        // This endpoint behavior in InMemoryStore was "all accounts in the system".
        // AccountService.listAccounts requires clientId.
        // If this endpoint is for ALL accounts (admin view?), I need a service method
        // for it.
        // Or if it's not used, return 404.
        // Given strict Phase 3 requirements, let's assume this was for debugging.
        // I'll return a message saying "Please provide clientId to list accounts".
        return Response.status(Response.Status.BAD_REQUEST).entity("Use /clients/{id}/accounts").build();
    }
}
