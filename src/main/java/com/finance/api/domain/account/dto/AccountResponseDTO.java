package com.finance.api.domain.account.dto;

import com.finance.api.domain.account.Account;
import java.math.BigDecimal;

public record AccountResponseDTO(
        String id,
        String name,
        String type,
        BigDecimal initialBalance,
        BigDecimal currentBalance,
        String color,
        Boolean active
) {
    public AccountResponseDTO(Account account) {
        this(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getInitialBalance(),
                account.getCurrentBalance(),
                account.getColor(),
                account.getActive()
        );
    }
}