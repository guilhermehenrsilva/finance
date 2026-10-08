package com.finance.api.domain.transaction.dto;

import com.finance.api.domain.transaction.Transaction;
import com.finance.api.domain.transaction.TransactionStatus;
import com.finance.api.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponseDTO(
        String id,
        String accountName,
        String creditCardName,
        String invoiceId,
        String description,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        LocalDate date,
        String categoryName
) {
    public TransactionResponseDTO(Transaction t) {
        this(
                t.getId(),
                t.getAccount() != null ? t.getAccount().getName() : null,
                t.getCreditCard() != null ? t.getCreditCard().getName() : null,
                t.getInvoice() != null ? t.getInvoice().getId() : null,
                t.getDescription(),
                t.getAmount(),
                t.getType(),
                t.getStatus(),
                t.getDate(),
                t.getCategory() != null ? t.getCategory().getName() : null
        );
    }
}