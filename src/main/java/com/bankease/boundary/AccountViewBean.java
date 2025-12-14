package com.bankease.boundary;

import ch.unil.doplab.bankease.domain.Client;
import ch.unil.doplab.bankease.service.AccountService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Named
@RequestScoped
public class AccountViewBean implements Serializable {

    @Inject
    private AccountService accountService;

    @Inject
    private LoginBean loginBean;

    private List<Map<String, Object>> accounts;
    private Map<String, Object> summary;

    @PostConstruct
    public void init() {
        refresh();
    }

    // Recharge toutes les données du dashboard
    public void refresh() {
        Client logged = loginBean.getLoggedClient();
        if (logged == null) {
            accounts = Collections.emptyList();
            summary = Collections.emptyMap();
            return;
        }

        String clientId = logged.getId();
        accounts = accountService.listAccounts(clientId);
        summary = accountService.totalBalance(clientId);
    }

    public List<Map<String, Object>> getAccounts() {
        return accounts;
    }

    public Map<String, Object> getSummary() {
        return summary;
    }
}
