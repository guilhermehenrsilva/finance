package com.finance.api.domain.creditcard.dto;

import com.finance.api.domain.creditcard.CreditCard;
import java.math.BigDecimal;

public record CreditCardResponseDTO(
        String id,
        String name,
        String accountId,
        String accountName,
        BigDecimal creditLimit,
        BigDecimal availableLimit,
        Integer closingDay,
        Integer dueDay,
        String color,
        Boolean active
) {
    public CreditCardResponseDTO(CreditCard card) {
        this(
                card.getId(),
                card.getName(),
                card.getAccount() != null ? card.getAccount().getId() : null,
                card.getAccount() != null ? card.getAccount().getName() : null,
                card.getCreditLimit(),
                card.getAvailableLimit(),
                card.getClosingDay(),
                card.getDueDay(),
                card.getColor(),
                card.getActive()
        );
    }
}