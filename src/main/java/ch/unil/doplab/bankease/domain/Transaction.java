package ch.unil.doplab.bankease.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Transaction {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private BigDecimal amount;

    private String description;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @ManyToOne
    private Account source;

    @ManyToOne
    private Account destination;

    @ManyToOne
    private Client client;

    public Transaction() {
    }

    public Transaction(
            TransactionType type,
            BigDecimal amount,
            String description,
            Account source,
            Account destination) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.source = source;
        this.destination = destination;
        this.status = TransactionStatus.COMPLETED;
        this.timestamp = LocalDateTime.now();
    }

    // --- Business Logic ---

    public void cancel() {
        this.status = TransactionStatus.CANCELED;
    }

    // --- Getters / Setters ---

    public String getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Account getSource() {
        return source;
    }

    public Account getDestination() {
        return destination;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public void markApproved() {
        this.status = TransactionStatus.COMPLETED; // Or APPROVED if exists
    }

    public void markRejected() {
        this.status = TransactionStatus.CANCELED; // Or REJECTED if exists
    }

    // --- Static Factories ---

    public static Transaction createDeposit(Account destination, BigDecimal amount, String description) {
        Transaction tx = new Transaction();
        tx.id = UUID.randomUUID().toString();
        tx.type = TransactionType.DEPOSIT;
        tx.amount = amount; // Maybe check positive?
        tx.description = description;
        tx.destination = destination;
        tx.status = TransactionStatus.COMPLETED;
        tx.timestamp = LocalDateTime.now();
        return tx;
    }

    public static Transaction createWithdrawal(Account source, BigDecimal amount, String description) {
        Transaction tx = new Transaction();
        tx.id = UUID.randomUUID().toString();
        tx.type = TransactionType.WITHDRAWAL;
        tx.amount = amount;
        tx.description = description;
        tx.source = source;
        tx.status = TransactionStatus.COMPLETED;
        tx.timestamp = LocalDateTime.now();
        return tx;
    }

    public static Transaction createTransfer(Account source, Account destination, BigDecimal amount, String description,
            TransactionStatus status) {
        Transaction tx = new Transaction();
        tx.id = UUID.randomUUID().toString();
        tx.type = TransactionType.TRANSFER;
        tx.amount = amount;
        tx.description = description;
        tx.source = source;
        tx.destination = destination;
        tx.status = status;
        tx.timestamp = LocalDateTime.now();
        return tx;
    }
}