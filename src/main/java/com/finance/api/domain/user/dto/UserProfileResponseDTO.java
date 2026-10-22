package com.finance.api.domain.user.dto;

import com.finance.api.domain.user.User;
import java.time.LocalDateTime;

public record UserProfileResponseDTO(
        String id,
        String name,
        String email,
        String mainCurrency,
        LocalDateTime createdAt
) {
    public UserProfileResponseDTO(User user) {
        this(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMainCurrency(),
                user.getCreatedAt()
        );
    }
}
