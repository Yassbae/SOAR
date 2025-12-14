package ch.unil.doplab.bankease.config;

import ch.unil.doplab.bankease.domain.Account;
import ch.unil.doplab.bankease.domain.AccountType;
import ch.unil.doplab.bankease.domain.Client;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initialise la base MySQL au démarrage :
 * - crée le schéma (via JPA)
 * - insère des clients NON activés avec PIN
 * - les clients activeront leur compte via le formulaire Register
 */
@Singleton
@Startup
public class DatabaseInitializer {

    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();

    @PostConstruct
    public void init() {
        LOGGER.info("Starting DatabaseInitializer (PIN-based accounts)...");

        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {
            emf = Persistence.createEntityManagerFactory("bankeasePU");
            em = emf.createEntityManager();
            em.getTransaction().begin();

            // ================== CLIENTS DE TEST ==================
            for (int i = 1; i <= 5; i++) {
                String pin = generatePin();

                Client c = new Client(pin);

                Account acc = c.openAccount(AccountType.CURRENT);
                c.deposit(acc, new BigDecimal("1000.00"), "Initial deposit");

                em.persist(c);

                LOGGER.info("Test client created with PIN: " + pin);
            }

            // ================== CLIENTS BONUS (PHASE 3) ==================
            for (int i = 0; i < 1000; i++) {
                String pin = generatePin();

                Client c = new Client(pin);

                Account acc = c.openAccount(AccountType.CURRENT);
                c.deposit(acc, new BigDecimal("100.00"), "Initial bonus deposit");

                em.persist(c);

                if (i > 0 && i % 100 == 0) {
                    LOGGER.info("Inserted " + i + " PIN-only clients...");
                }
            }

            em.getTransaction().commit();
            LOGGER.info("DatabaseInitializer finished successfully.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during database initialization", e);
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }
    }

    // ================== UTILITAIRE ==================

    private String generatePin() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
