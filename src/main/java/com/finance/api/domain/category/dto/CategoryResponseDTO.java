package com.finance.api.domain.category.dto;

import com.finance.api.domain.category.Category;
import com.finance.api.domain.transaction.TransactionType;

public record CategoryResponseDTO(
        String id,
        String name,
        TransactionType type,
        String icon,
        String color,
        String parentId,
        String parentName
) {
    public CategoryResponseDTO(Category c) {
        this(
                c.getId(),
                c.getName(),
                c.getType(),
                c.getIcon(),
                c.getColor(),
                c.getParentCategory() != null ? c.getParentCategory().getId() : null,
                c.getParentCategory() != null ? c.getParentCategory().getName() : null
        );
    }
}