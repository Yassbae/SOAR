package ch.unil.doplab.bankease.service;

import ch.unil.doplab.bankease.dto.DepositRequest;
import ch.unil.doplab.bankease.dto.TransferRequest;
import ch.unil.doplab.bankease.dto.WithdrawRequest;

import java.util.List;
import java.util.Map;

public interface TransactionService {
    Map<String, Object> deposit(DepositRequest req);
    Map<String, Object> withdraw(WithdrawRequest req);
    Map<String, Object> transfer(TransferRequest req);
    Map<String, Object> cancel(String txId);
    List<Map<String, Object>> historyByClient(String clientId);
}
