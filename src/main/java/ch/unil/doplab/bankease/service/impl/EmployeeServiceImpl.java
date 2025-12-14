package ch.unil.doplab.bankease.service.impl;

import ch.unil.doplab.bankease.domain.Employee;
import ch.unil.doplab.bankease.domain.Transaction;
import ch.unil.doplab.bankease.domain.TransactionStatus;
import ch.unil.doplab.bankease.exception.ApiException;
import ch.unil.doplab.bankease.service.EmployeeService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EmployeeServiceImpl implements EmployeeService {

    @Inject
    private EntityManager em;

    @Override
    public Map<String, Object> approve(String employeeId, String txId) {
        try {
            em.getTransaction().begin();

            Employee emp = em.find(Employee.class, employeeId);
            if (emp == null)
                throw new ApiException(404, "Employee not found");

            Transaction tx = em.find(Transaction.class, txId);
            if (tx == null)
                throw new ApiException(404, "Transaction not found");

            emp.approve(tx);

            // Persist (merge if needed, but managing transaction is enough to flush dirty
            // checking)
            // But tx status is modified, so JPA dirty checking handles it.

            em.getTransaction().commit();
            return txToMap(tx);

        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            if (e instanceof ApiException)
                throw e;
            throw new RuntimeException("Approval failed", e);
        }
    }

    @Override
    public Map<String, Object> reject(String employeeId, String txId) {
        try {
            em.getTransaction().begin();

            Employee emp = em.find(Employee.class, employeeId);
            if (emp == null)
                throw new ApiException(404, "Employee not found");

            Transaction tx = em.find(Transaction.class, txId);
            if (tx == null)
                throw new ApiException(404, "Transaction not found");

            emp.reject(tx);

            em.getTransaction().commit();
            return txToMap(tx);

        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            if (e instanceof ApiException)
                throw e;
            throw new RuntimeException("Rejection failed", e);
        }
    }

    @Override
    public List<Map<String, Object>> pendingApprovals() {
        List<Transaction> txs = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.status = :status", Transaction.class)
                .setParameter("status", TransactionStatus.PENDING_APPROVAL)
                .getResultList();

        return txs.stream()
                .map(this::txToMap)
                .toList();
    }

    private Map<String, Object> txToMap(Transaction t) {
        String src = t.getSource() == null ? "-" : t.getSource().getAccountNumber();
        String dst = t.getDestination() == null ? "-" : t.getDestination().getAccountNumber();
        return Map.of(
                "id", t.getId(),
                "type", t.getType().name(),
                "amount", t.getAmount(),
                "source", src,
                "destination", dst,
                "status", t.getStatus().name(),
                "timestamp", t.getTimestamp().toString(),
                "description", t.getDescription());
    }
}
