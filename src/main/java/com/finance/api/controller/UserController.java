package com.finance.api.controller;

import com.finance.api.domain.user.User;
import com.finance.api.domain.user.UserService;
import com.finance.api.domain.user.dto.ChangePasswordRequestDTO;
import com.finance.api.domain.user.dto.UpdateProfileRequestDTO;
import com.finance.api.domain.user.dto.UserProfileResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getProfile(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateProfileRequestDTO dto
    ) {
        return ResponseEntity.ok(userService.updateProfile(user, dto));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ChangePasswordRequestDTO dto
    ) {
        userService.changePassword(user, dto);
        return ResponseEntity.noContent().build();
    }
}
