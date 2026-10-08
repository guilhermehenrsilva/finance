package com.finance.api.domain.transaction.dto;

import com.finance.api.domain.transaction.RecurringFrequency;
import com.finance.api.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTransactionRequestDTO(
        String description,
        BigDecimal amount,
        TransactionType type,
        RecurringFrequency frequency,
        LocalDate startDate,
        LocalDate endDate,
        String categoryId,
        String accountId,
        String creditCardId
) {}