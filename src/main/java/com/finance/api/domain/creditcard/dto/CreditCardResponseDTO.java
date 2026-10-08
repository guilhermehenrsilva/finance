package com.finance.api.domain.creditcard.dto;

import com.finance.api.domain.creditcard.CreditCard;
import java.math.BigDecimal;

public record CreditCardResponseDTO(
        String id,
        String name,
        String accountName,
        BigDecimal creditLimit,
        BigDecimal availableLimit,
        Integer closingDay,
        Integer dueDay,
        String color
) {
    public CreditCardResponseDTO(CreditCard card) {
        this(
                card.getId(),
                card.getName(),
                card.getAccount() != null ? card.getAccount().getName() : null,
                card.getCreditLimit(),
                card.getAvailableLimit(),
                card.getClosingDay(),
                card.getDueDay(),
                card.getColor()
        );
    }
}