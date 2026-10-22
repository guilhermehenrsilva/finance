package com.finance.api.domain.tag.dto;

import com.finance.api.domain.tag.Tag;

public record TagResponseDTO(
        String id,
        String name,
        String color
) {
    public TagResponseDTO(Tag tag) {
        this(
                tag.getId(),
                tag.getName(),
                tag.getColor()
        );
    }
}
