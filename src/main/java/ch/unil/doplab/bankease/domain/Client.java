package ch.unil.doplab.bankease.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "clients")
public class Client extends User {

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // ================== CONSTRUCTEURS ==================

    protected Client() {
        // requis par JPA
    }

    /**
     * Création INITIAL du client par l'ADMIN
     * → uniquement avec un PIN
     * → compte NON activé
     */
    public Client(String pin) {
        super(pin);
    }

    // ================== ACTIVATION DU COMPTE ==================

    /**
     * Activation du compte lors du REGISTER côté client.
     * Le PIN existe déjà en base, on complète les infos.
     */
    public void activateAccount(String username,
                                String password,
                                String firstName,
                                String lastName,
                                String email,
                                String phoneNumber) {

        if (isActivated()) {
            throw new IllegalStateException("Account already activated");
        }

        this.setUsername(Objects.requireNonNull(username));
        this.setPassword(Objects.requireNonNull(password));
        this.setFirstName(Objects.requireNonNull(firstName));
        this.setLastName(Objects.requireNonNull(lastName));
        this.setEmail(Objects.requireNonNull(email));
        this.setPhoneNumber(Objects.requireNonNull(phoneNumber));
        this.setActivated(true);
    }

    // ================== RELATIONS ==================

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    // ================== MÉTIER BANCAIRE ==================

    public Account openAccount(AccountType type) {
        Objects.requireNonNull(type, "Account type cannot be null");
        Account account = new Account(this, type);
        accounts.add(account);
        return account;
    }

    public void deposit(Account destination, BigDecimal amount, String description) {
        ensureOwns(destination);
        Objects.requireNonNull(amount, "amount");

        destination.receiveDeposit(amount);

        Transaction tx = Transaction.createDeposit(destination, amount, description);
        transactions.add(tx);
        destination.attachTransaction(tx);
    }

    public void withdraw(Account source, BigDecimal amount, String description) {
        ensureOwns(source);
        Objects.requireNonNull(amount, "amount");

        source.processWithdrawal(amount);

        Transaction tx = Transaction.createWithdrawal(source, amount, description);
        transactions.add(tx);
        source.attachTransaction(tx);
    }

    public Transaction transfer(Account source,
                                Account destination,
                                BigDecimal amount,
                                String description) {

        ensureOwns(source);
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(amount, "amount");

        if (source == destination) {
            throw new IllegalArgumentException("Source and destination accounts must differ.");
        }

        source.processWithdrawal(amount);
        destination.receiveDeposit(amount);

        Transaction tx = Transaction.createTransfer(
                source,
                destination,
                amount,
                description,
                TransactionStatus.COMPLETED);

        transactions.add(tx);
        source.attachTransaction(tx);
        destination.attachTransaction(tx);

        return tx;
    }

    // ================== UTILITAIRES ==================

    public BigDecimal getTotalBalance() {
        return accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Transaction> viewTransactionHistory() {
        return List.copyOf(transactions);
    }

    public void requestCancellation(Transaction tx) {
        if (tx.getStatus() != TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Only completed transactions can be canceled directly.");
        }
        tx.cancel();
    }

    private void ensureOwns(Account account) {
        if (account == null || !accounts.contains(account)) {
            throw new IllegalArgumentException("Client does not own the specified account.");
        }
    }
}
