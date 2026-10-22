package com.finance.api.domain.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequestDTO(
        @NotBlank(message = "O nome da tag é obrigatório")
        @Size(max = 50, message = "O nome não pode exceder 50 caracteres")
        String name,

        String color
) {}
