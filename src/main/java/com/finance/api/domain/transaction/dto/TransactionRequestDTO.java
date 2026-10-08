package com.finance.api.domain.transaction.dto;

import com.finance.api.domain.transaction.TransactionStatus;
import com.finance.api.domain.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequestDTO(
        String description,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        LocalDate date,
        String categoryId,
        String accountId,
        String creditCardId,
        Integer totalInstallments 
) {}