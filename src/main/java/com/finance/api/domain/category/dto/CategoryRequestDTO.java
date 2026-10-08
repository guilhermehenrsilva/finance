package com.finance.api.domain.category.dto;

import com.finance.api.domain.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequestDTO(
        @NotBlank String name,
        @NotNull TransactionType type,
        String icon,
        String color,
        String parentId // Pode ser null se for uma categoria principal
) {}