package ch.unil.doplab.bankease.service;

import ch.unil.doplab.bankease.domain.Client;

public interface ClientService {

    /**
     * Activation d’un compte client via PIN.
     * Le client existe déjà en base (créé par l’admin).
     */
    Client activateAccount(
            String pin,
            String username,
            String password,
            String firstName,
            String lastName,
            String email,
            String phoneNumber
    );

    /**
     * Validation du login (uniquement si compte activé).
     */
    Client validateLogin(String username, String password);
}
