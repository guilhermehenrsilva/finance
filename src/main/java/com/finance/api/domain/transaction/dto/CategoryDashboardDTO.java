package com.finance.api.domain.transaction.dto;

import java.math.BigDecimal;

public record CategoryDashboardDTO(
        String categoryName,
        BigDecimal amount
) {
}
