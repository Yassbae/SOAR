package ch.unil.doplab.bankease.service.impl;

import ch.unil.doplab.bankease.domain.*;
import ch.unil.doplab.bankease.dto.DepositRequest;
import ch.unil.doplab.bankease.dto.TransferRequest;
import ch.unil.doplab.bankease.dto.WithdrawRequest;
import ch.unil.doplab.bankease.exception.ApiException;
import ch.unil.doplab.bankease.service.TransactionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TransactionServiceImpl implements TransactionService {

    @Inject
    EntityManager em;

    // ===============================
    // DEPOSIT
    // ===============================

    @Override
    public Map<String, Object> deposit(DepositRequest req) {
        try {
            em.getTransaction().begin();

            Client c = em.find(Client.class, req.clientId());
            if (c == null)
                throw new ApiException(404, "Client not found");

            Account acc = em.find(Account.class, req.accountNumber());
            if (acc == null)
                throw new ApiException(404, "Account not found");

            // Update balance
            acc.setBalance(acc.getBalance().add(req.amount()));

            // Create transaction
            Transaction tx = new Transaction(
                    TransactionType.DEPOSIT,
                    req.amount(),
                    req.description(),
                    acc,
                    null);

            tx.setClient(c);

            em.persist(tx);

            em.getTransaction().commit();

            Map<String, Object> map = txToMap(tx);
            map.put("balance", acc.getBalance());
            return map;

        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            if (e instanceof ApiException)
                throw e;
            throw new RuntimeException("Deposit failed", e);
        }
    }

    // ===============================
    // WITHDRAW
    // ===============================

    @Override
    public Map<String, Object> withdraw(WithdrawRequest req) {
        try {
            em.getTransaction().begin();

            Client c = em.find(Client.class, req.clientId());
            if (c == null)
                throw new ApiException(404, "Client not found");

            Account acc = em.find(Account.class, req.accountNumber());
            if (acc == null)
                throw new ApiException(404, "Account not found");

            if (acc.getBalance().compareTo(req.amount()) < 0) {
                throw new ApiException(400, "Insufficient balance");
            }

            acc.setBalance(acc.getBalance().subtract(req.amount()));

            Transaction tx = new Transaction(
                    TransactionType.WITHDRAWAL,
                    req.amount(),
                    req.description(),
                    acc,
                    null);

            tx.setClient(c);

            em.persist(tx);

            em.getTransaction().commit();

            Map<String, Object> map = txToMap(tx);
            map.put("balance", acc.getBalance());
            return map;

        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            if (e instanceof ApiException)
                throw e;
            throw new RuntimeException("Withdraw failed", e);
        }
    }

    // ===============================
    // TRANSFER
    // ===============================

    @Override
    public Map<String, Object> transfer(TransferRequest req) {
        try {
            em.getTransaction().begin();

            Client c = em.find(Client.class, req.clientId());
            if (c == null)
                throw new ApiException(404, "Client not found");

            Account src = em.find(Account.class, req.sourceAccountNumber());
            Account dst = em.find(Account.class, req.destinationAccountNumber());

            if (src == null || dst == null) {
                throw new ApiException(404, "Account not found");
            }

            if (src.getBalance().compareTo(req.amount()) < 0) {
                throw new ApiException(400, "Insufficient balance");
            }

            src.setBalance(src.getBalance().subtract(req.amount()));
            dst.setBalance(dst.getBalance().add(req.amount()));

            Transaction tx = new Transaction(
                    TransactionType.TRANSFER,
                    req.amount(),
                    req.description(),
                    src,
                    dst);

            tx.setClient(c);
            em.persist(tx);

            em.getTransaction().commit();

            Map<String, Object> map = txToMap(tx);
            map.put("sourceBalance", src.getBalance());
            map.put("destinationBalance", dst.getBalance());
            return map;

        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            if (e instanceof ApiException)
                throw e;
            throw new RuntimeException("Transfer failed", e);
        }
    }

    // ===============================
    // CANCEL TRANSACTION
    // ===============================

    @Override
    public Map<String, Object> cancel(String txId) {
        try {
            em.getTransaction().begin();

            Transaction tx = em.find(Transaction.class, txId);
            if (tx == null)
                throw new ApiException(404, "Transaction not found");

            if (tx.getStatus() != TransactionStatus.COMPLETED) {
                throw new ApiException(400, "Only completed transactions can be canceled");
            }

            tx.setStatus(TransactionStatus.CANCELED);

            // Revert changes? Logic not specified in original code, it just changed status.
            // If strictly banking, we should refund. But for now keep as is.

            em.getTransaction().commit();

            return txToMap(tx);

        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            if (e instanceof ApiException)
                throw e;
            throw new RuntimeException("Cancel failed", e);
        }
    }

    // ===============================
    // HISTORY
    // ===============================

    @Override
    public List<Map<String, Object>> historyByClient(String clientId) {

        List<Transaction> txs = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.client.id = :cid ORDER BY t.timestamp DESC",
                Transaction.class)
                .setParameter("cid", clientId)
                .getResultList();

        return txs.stream().map(this::txToMap).toList();
    }

    // ===============================
    // UTILS
    // ===============================

    private Map<String, Object> txToMap(Transaction t) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", t.getId());
        map.put("type", t.getType().name());
        map.put("amount", t.getAmount());
        map.put("description", t.getDescription());
        map.put("timestamp", t.getTimestamp().toString());
        map.put("status", t.getStatus().name());

        map.put("source", t.getSource() == null ? "-" : t.getSource().getAccountNumber());
        map.put("destination", t.getDestination() == null ? "-" : t.getDestination().getAccountNumber());

        return map;
    }
}