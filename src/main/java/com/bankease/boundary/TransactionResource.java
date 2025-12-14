package com.bankease.boundary;

import ch.unil.doplab.bankease.dto.DepositRequest;
import ch.unil.doplab.bankease.dto.TransferRequest;
import ch.unil.doplab.bankease.dto.WithdrawRequest;
import ch.unil.doplab.bankease.exception.ApiException;
import ch.unil.doplab.bankease.service.AccountService;
import ch.unil.doplab.bankease.service.TransactionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@ApplicationScoped
@Path("/transactions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource {

        @Inject
        TransactionService transactionService;

        @Inject
        AccountService accountService;

        // --------- DEPOSIT ----------
        @POST
        @Path("/deposit")
        public Response deposit(Map<String, Object> body) {
                try {
                        String account = String.valueOf(body.get("accountNumber"));
                        BigDecimal amount = new BigDecimal(body.get("amount").toString());

                        // Resolve Client ID
                        Map<String, Object> acc = accountService.getAccount(account);
                        String clientId = (String) acc.get("clientId");

                        DepositRequest req = new DepositRequest(clientId, account, amount, "Deposit via REST");
                        Map<String, Object> result = transactionService.deposit(req);

                        return Response.ok(Map.of(
                                        "status", "OK",
                                        "op", "deposit",
                                        "accountNumber", account,
                                        "amount", amount,
                                        "newBalance", result.get("balance"))).build();
                } catch (ApiException e) {
                        return Response.status(e.getStatus()).entity(Map.of("error", e.getMessage())).build();
                } catch (Exception e) {
                        return Response.serverError().entity(Map.of("error", e.getMessage())).build();
                }
        }

        // --------- WITHDRAW ----------
        @POST
        @Path("/withdraw")
        public Response withdraw(Map<String, Object> body) {
                try {
                        String account = String.valueOf(body.get("accountNumber"));
                        BigDecimal amount = new BigDecimal(body.get("amount").toString());

                        Map<String, Object> acc = accountService.getAccount(account);
                        String clientId = (String) acc.get("clientId");

                        WithdrawRequest req = new WithdrawRequest(clientId, account, amount, "Withdraw via REST");
                        Map<String, Object> result = transactionService.withdraw(req);

                        return Response.ok(Map.of(
                                        "status", "OK",
                                        "op", "withdraw",
                                        "accountNumber", account,
                                        "amount", amount,
                                        "newBalance", result.get("balance"))).build();
                } catch (ApiException e) {
                        return Response.status(e.getStatus()).entity(Map.of("error", e.getMessage())).build();
                } catch (Exception e) {
                        return Response.serverError().entity(Map.of("error", e.getMessage())).build();
                }
        }

        // --------- TRANSFER ----------
        @POST
        @Path("/transfer")
        public Response transfer(Map<String, Object> body) {
                try {
                        String from = String.valueOf(body.getOrDefault("fromAccount",
                                        body.getOrDefault("sourceAccount", body.get("from"))));
                        String to = String.valueOf(body.getOrDefault("toAccount",
                                        body.getOrDefault("destinationAccount", body.get("to"))));
                        BigDecimal amount = new BigDecimal(body.get("amount").toString());

                        Map<String, Object> acc = accountService.getAccount(from);
                        String clientId = (String) acc.get("clientId");

                        TransferRequest req = new TransferRequest(clientId, from, to, amount, "Transfer via REST");
                        Map<String, Object> result = transactionService.transfer(req);

                        return Response.ok(Map.of(
                                        "status", "OK",
                                        "op", "transfer",
                                        "fromAccount", from,
                                        "toAccount", to,
                                        "amount", amount,
                                        "newBalanceFrom", result.get("sourceBalance"),
                                        "newBalanceTo", result.get("destinationBalance"))).build();
                } catch (ApiException e) {
                        return Response.status(e.getStatus()).entity(Map.of("error", e.getMessage())).build();
                } catch (Exception e) {
                        return Response.serverError().entity(Map.of("error", e.getMessage())).build();
                }
        }

        // --------- EXCHANGE (free plan: base EUR only) ----------
        // Compute CHF->{CURRENCY} = (EUR->{CURRENCY}) / (EUR->CHF)
        @GET
        @Path("/exchange/{currency}")
        public Response getExchangeRate(@PathParam("currency") String currency) {
                String target = currency.toUpperCase();
                String accessKey = System.getenv().getOrDefault(
                                "EXCHANGERATES_API_KEY",
                                "988d5995c35fa0902752097116adf294");

                String apiUrl = "https://api.exchangeratesapi.io/v1/latest"
                                + "?access_key=" + accessKey
                                + "&symbols=CHF," + target; // base=EUR on free plan

                try {
                        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                        conn.setRequestMethod("GET");
                        conn.connect();

                        InputStream is = conn.getResponseCode() < 300
                                        ? conn.getInputStream()
                                        : conn.getErrorStream();

                        try (JsonReader jr = Json.createReader(is)) {
                                JsonObject root = jr.readObject();
                                if (root.containsKey("error")) {
                                        return Response.status(Response.Status.BAD_GATEWAY)
                                                        .entity(Map.of("source", "https://api.exchangeratesapi.io/v1",
                                                                        "error", root.get("error")))
                                                        .build();
                                }
                                JsonObject rates = root.getJsonObject("rates");
                                double eurToChf = rates.getJsonNumber("CHF").doubleValue();
                                double eurToTarget = rates.getJsonNumber(target).doubleValue();
                                double chfToTarget = eurToTarget / eurToChf;

                                return Response.ok(Map.of(
                                                "base", "CHF",
                                                "currency", target,
                                                "rate", chfToTarget,
                                                "explanation", "rate = (EUR→" + target + ") / (EUR→CHF)",
                                                "source", "https://api.exchangeratesapi.io/v1",
                                                "date", root.getString("date", ""))).build();
                        }
                } catch (Exception e) {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(Map.of("error", e.getMessage()))
                                        .build();
                }
        }

        // --------- BALANCE helper ----------
        @GET
        @Path("/balance/{account}")
        public Response getBalance(@PathParam("account") String account) {
                try {
                        Map<String, Object> acc = accountService.getAccount(account);
                        return Response.ok(Map.of(
                                        "accountNumber", account,
                                        "balance", acc.get("balance"))).build();
                } catch (ApiException e) {
                        return Response.status(e.getStatus()).entity(Map.of("error", e.getMessage())).build();
                }
        }
}
