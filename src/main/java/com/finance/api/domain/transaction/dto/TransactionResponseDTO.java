package com.finance.api.domain.transaction.dto;

import com.finance.api.domain.tag.dto.TagResponseDTO;
import com.finance.api.domain.transaction.Transaction;
import com.finance.api.domain.transaction.TransactionStatus;
import com.finance.api.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record TransactionResponseDTO(
        String id,
        String accountId,
        String accountName,
        String categoryId,
        String creditCardName,
        String invoiceId,
        String description,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        LocalDate date,
        String categoryName,
        List<TagResponseDTO> tags
) {
    public TransactionResponseDTO(Transaction t) {
        this(
                t.getId(),
                t.getAccount() != null ? t.getAccount().getId() : null,
                t.getAccount() != null ? t.getAccount().getName() : null,
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getCreditCard() != null ? t.getCreditCard().getName() : null,
                t.getInvoice() != null ? t.getInvoice().getId() : null,
                t.getDescription(),
                t.getAmount(),
                t.getType(),
                t.getStatus(),
                t.getDate(),
                t.getCategory() != null ? t.getCategory().getName() : null,
                t.getTags() != null ? t.getTags().stream().map(TagResponseDTO::new).collect(Collectors.toList()) : List.of()
        );
    }
}