package ch.unil.doplab.bankease.service;

import ch.unil.doplab.bankease.dto.CreateAccountRequest;

import java.util.List;
import java.util.Map;

public interface AccountService {
    Map<String, Object> openAccount(String clientId, CreateAccountRequest req);

    List<Map<String, Object>> listAccounts(String clientId);

    Map<String, Object> getAccount(String accountNumber);

    Map<String, Object> totalBalance(String clientId);
}
