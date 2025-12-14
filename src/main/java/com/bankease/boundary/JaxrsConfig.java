package com.bankease.boundary;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class JaxrsConfig extends Application {
    // vide: Payara découvrira automatiquement les ressources JAX-RS
}
