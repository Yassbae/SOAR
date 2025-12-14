package com.bankease.boundary;

import ch.unil.doplab.bankease.service.TransactionService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@RequestScoped
public class TransactionHistoryBean implements Serializable {

    @Inject
    private LoginBean loginBean;

    @Inject
    private TransactionService transactionService;

    private List<Map<String, Object>> history;

    public List<Map<String, Object>> getHistory() {
        if (history == null && loginBean.getLoggedClient() != null) {
            String clientId = loginBean.getLoggedClient().getId();
            history = transactionService.historyByClient(clientId);
        }
        return history;
    }

    public List<Map<String, Object>> getRecent() {
        if (getHistory() == null)
            return List.of();
        return history.stream().limit(5).collect(Collectors.toList());
    }

}
