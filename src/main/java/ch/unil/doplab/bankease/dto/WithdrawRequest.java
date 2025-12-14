package ch.unil.doplab.bankease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record WithdrawRequest(
        @NotBlank String clientId,
        @NotBlank String accountNumber,
        @NotNull @Positive BigDecimal amount,
        String description
) {}
