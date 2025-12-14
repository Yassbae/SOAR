package com.bankease.boundary;

import ch.unil.doplab.bankease.domain.Client;
import ch.unil.doplab.bankease.service.ClientService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;

@Named
@SessionScoped
public class LoginBean implements Serializable {

    private String username;
    private String password;
    private Client loggedClient;

    @Inject
    private ClientService clientService;

    // === GETTERS / SETTERS ===

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Client getLoggedClient() {
        return loggedClient;
    }

    public void setLoggedClient(Client loggedClient) {
        this.loggedClient = loggedClient;
    }


    public String login() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        // Vérification des champs vides
        if (username == null || username.isBlank() ||
                password == null || password.isBlank()) {

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Username or password missing",
                    "Please fill in both fields."
            ));
            return null; // reste sur la page de login
        }

        Client c = clientService.validateLogin(username, password);

        if (c == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Invalid username or password",
                    "Please check your credentials."
            ));
            return null;
        }


        loggedClient = c;
        password = null;

        return "dashboard?faces-redirect=true";
    }

    public String logout() {
        FacesContext.getCurrentInstance()
                .getExternalContext()
                .invalidateSession();

        return "index.xhtml?faces-redirect=true";
    }
}
