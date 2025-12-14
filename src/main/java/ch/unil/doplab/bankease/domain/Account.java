package ch.unil.doplab.bankease.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class Account {

    @Id
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @ManyToOne
    private Client client;

    public Account() {
        this.accountNumber = UUID.randomUUID().toString();
        this.balance = BigDecimal.ZERO;
    }

    public Account(Client client, AccountType type) {
        this();
        this.client = client;
        this.type = type;
    }

    // --- Getters / Setters ---

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client c) {
        this.client = c;
    }

    public void receiveDeposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void processWithdrawal(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void attachTransaction(Transaction tx) {
        // This might be setting the side of relationship, but Client seems to handle
        // the collection add.
        // This method was likely intended to do nothing or handle bidirectional logic
        // if needed.
        // For now, empty implementation or simple validation to satisfy compilation.
    }

    // --- Helpers (Legacy/Internal) ---

    public void add(BigDecimal amount) {
        receiveDeposit(amount);
    }

    public boolean subtract(BigDecimal amount) {
        try {
            processWithdrawal(amount);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}