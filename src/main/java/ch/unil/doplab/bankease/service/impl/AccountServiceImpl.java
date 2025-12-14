package ch.unil.doplab.bankease.service.impl;

import ch.unil.doplab.bankease.domain.*;
import ch.unil.doplab.bankease.dto.CreateAccountRequest;
import ch.unil.doplab.bankease.exception.ApiException;
import ch.unil.doplab.bankease.service.AccountService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Named("accountService")
public class AccountServiceImpl implements AccountService {

    @Inject
    private EntityManager em;

    @Override
    public Map<String, Object> openAccount(String clientId, CreateAccountRequest req) {
        // Validate inputs
        AccountType type;
        try {
            type = AccountType.valueOf(req.type());
        } catch (Exception e) {
            throw new ApiException(400, "Invalid account type");
        }

        try {
            em.getTransaction().begin();

            Client client = em.find(Client.class, clientId);
            if (client == null) {
                throw new ApiException(404, "Client not found");
            }

            // Create account
            Account acc = client.openAccount(type);

            // Persist changes
            em.merge(client); // Cascade should handle the new account

            em.getTransaction().commit();

            Map<String, Object> map = new HashMap<>();
            map.put("clientId", client.getId());
            map.put("accountNumber", acc.getAccountNumber());
            map.put("type", acc.getType().name());
            map.put("balance", acc.getBalance());
            return map;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (e instanceof ApiException)
                throw e;
            throw new RuntimeException("Error opening account", e);
        }
    }

    @Override
    public List<Map<String, Object>> listAccounts(String clientId) {
        Client client = em.find(Client.class, clientId);
        if (client == null) {
            throw new ApiException(404, "Client not found");
        }

        // Force fetch if lazy? getAccounts is standard list access
        // Since we are traversing, better ensuring it's loaded.
        return client.getAccounts().stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("accountNumber", a.getAccountNumber());
            map.put("type", a.getType().name());
            map.put("balance", a.getBalance());
            return map;
        }).toList();
    }

    @Override
    public Map<String, Object> getAccount(String accountNumber) {
        Account a = em.find(Account.class, accountNumber);
        if (a == null) {
            throw new ApiException(404, "Account not found");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("clientId", a.getClient().getId());
        map.put("accountNumber", a.getAccountNumber());
        map.put("type", a.getType().name());
        map.put("balance", a.getBalance());
        return map;
    }

    @Override
    public Map<String, Object> totalBalance(String clientId) {
        Client client = em.find(Client.class, clientId);
        if (client == null) {
            throw new ApiException(404, "Client not found");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("clientId", client.getId());
        map.put("totalBalance", client.getTotalBalance());
        return map;
    }
}
