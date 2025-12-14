package com.bankease.boundary;

import ch.unil.doplab.bankease.dto.CreateAccountRequest;
import ch.unil.doplab.bankease.service.AccountService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;

@Named
@RequestScoped
public class CreateAccountBean implements Serializable {

    @Inject
    private AccountService accountService;

    @Inject
    private LoginBean loginBean;

    @Inject
    private AccountViewBean accountViewBean;

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String open() {
        try {
            String clientId = loginBean.getLoggedClient().getId();
            CreateAccountRequest req = new CreateAccountRequest(clientId, type);
            accountService.openAccount(clientId, req);

            accountViewBean.refresh();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Compte créé", "Le compte a bien été ouvert."));

            return "dashboard?faces-redirect=true";

        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur création compte", ex.getMessage()));
            return null;
        }
    }
}
