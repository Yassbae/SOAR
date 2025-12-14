package ch.unil.doplab.bankease.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees")
public class Employee extends User {

    public static final BigDecimal APPROVAL_THRESHOLD = new BigDecimal("5000.00");

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, nullable = false)
    private Role role;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private List<Client> managedClients = new ArrayList<>();

    protected Employee() {
        // JPA
    }

    public Employee(String pin,
                    String username,
                    String password,
                    String firstName,
                    String lastName,
                    String email,
                    String phoneNumber,
                    Role role) {

        super(pin);
        setUsername(username);
        setPassword(password);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setActivated(true);
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public List<Client> getManagedClients() {
        return managedClients;
    }

    // ================== APPROVAL LOGIC ==================

    public void approve(Transaction tx) {
        requireAdminOrAdvisor();

        if (tx.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Transaction is not pending approval.");
        }

        if (tx.getAmount().compareTo(APPROVAL_THRESHOLD) > 0) {
            throw new IllegalStateException("Transaction exceeds approval threshold.");
        }

        tx.markApproved();
    }

    public void reject(Transaction tx) {
        requireAdminOrAdvisor();

        if (tx.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Transaction is not pending approval.");
        }

        tx.markRejected();
    }

    private void requireAdminOrAdvisor() {
        if (role != Role.ADMINISTRATOR && role != Role.ADVISOR) {
            throw new SecurityException("Insufficient rights.");
        }
    }
}
