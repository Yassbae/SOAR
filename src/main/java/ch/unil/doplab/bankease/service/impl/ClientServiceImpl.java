package ch.unil.doplab.bankease.service.impl;

import ch.unil.doplab.bankease.domain.Client;
import ch.unil.doplab.bankease.exception.ApiException;
import ch.unil.doplab.bankease.service.ClientService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class ClientServiceImpl implements ClientService {

    @Inject
    private EntityManager em;

    /**
     * Activation du compte client via PIN.
     * Le client existe déjà en base (créé par l'admin).
     */
    @Override
    public Client activateAccount(
            String pin,
            String username,
            String password,
            String firstName,
            String lastName,
            String email,
            String phoneNumber
    ) {

        Client client;
        try {
            client = em.createQuery(
                            "SELECT c FROM Client c WHERE c.pin = :pin", Client.class)
                    .setParameter("pin", pin)
                    .getSingleResult();
        } catch (Exception e) {
            throw new ApiException(400, "Code PIN invalide");
        }

        if (client.isActivated()) {
            throw new ApiException(400, "Compte déjà activé");
        }

        em.getTransaction().begin();
        client.activateAccount(
                username,
                password,
                firstName,
                lastName,
                email,
                phoneNumber
        );
        em.getTransaction().commit();

        return client;
    }

    /**
     * Validation du login (uniquement si compte activé).
     */
    @Override
    public Client validateLogin(String username, String password) {

        Client client;
        try {
            client = em.createQuery(
                            "SELECT c FROM Client c WHERE c.username = :u", Client.class)
                    .setParameter("u", username)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }

        if (!client.isActivated()) {
            return null;
        }

        if (!client.getPassword().equals(password)) {
            return null;
        }

        return client;
    }
}
