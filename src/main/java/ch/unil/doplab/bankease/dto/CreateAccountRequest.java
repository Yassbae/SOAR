package ch.unil.doplab.bankease.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(
        @NotBlank String clientId,
        @NotBlank String type // CURRENT | SAVINGS | BUSINESS
) {}
