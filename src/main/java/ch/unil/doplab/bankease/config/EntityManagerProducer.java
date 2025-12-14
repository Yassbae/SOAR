package ch.unil.doplab.bankease.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@ApplicationScoped
public class EntityManagerProducer {

    private EntityManagerFactory emf;

    @PostConstruct
    public void init() {
        try {
            this.emf = Persistence.createEntityManagerFactory("bankeasePU");
        } catch (Throwable t) {
            System.err.println("CRITICAL ERROR: Failed to create EntityManagerFactory");
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public void closeEntityManager(@jakarta.enterprise.inject.Disposes EntityManager em) {
        if (em.isOpen()) {
            em.close();
        }
    }

    @PreDestroy
    public void closeFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
