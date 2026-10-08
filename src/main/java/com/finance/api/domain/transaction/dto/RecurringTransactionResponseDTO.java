package com.finance.api.domain.transaction.dto;

import com.finance.api.domain.transaction.RecurringFrequency;
import com.finance.api.domain.transaction.RecurringTransaction;
import com.finance.api.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTransactionResponseDTO(
        String id,
        String description,
        BigDecimal amount,
        TransactionType type,
        RecurringFrequency frequency,
        LocalDate nextDate,
        Boolean active
) {
    public RecurringTransactionResponseDTO(RecurringTransaction recurring) {
        this(
                recurring.getId(),
                recurring.getDescription(),
                recurring.getAmount(),
                recurring.getType(),
                recurring.getFrequency(),
                recurring.getNextDate(),
                recurring.getActive()
        );
    }
}