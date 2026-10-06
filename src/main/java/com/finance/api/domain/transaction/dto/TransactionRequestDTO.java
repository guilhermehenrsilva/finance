package com.finance.api.domain.transaction.dto;

import com.finance.api.domain.transaction.TransactionStatus;
import com.finance.api.domain.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequestDTO(
        @NotBlank String accountId,
        @NotBlank String description,
        @NotNull @Positive BigDecimal amount,
        @NotNull TransactionType type,
        @NotNull TransactionStatus status,
        @NotNull LocalDate date
) {}