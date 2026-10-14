package com.finance.api.controller;

import com.finance.api.domain.transaction.RecurringTransactionService;
import com.finance.api.domain.transaction.dto.RecurringTransactionRequestDTO;
import com.finance.api.domain.transaction.dto.RecurringTransactionResponseDTO;
import com.finance.api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-transactions")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringService;

    public RecurringTransactionController(RecurringTransactionService recurringService) {
        this.recurringService = recurringService;
    }

    @PostMapping
    public ResponseEntity<RecurringTransactionResponseDTO> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid RecurringTransactionRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recurringService.create(user, dto));
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponseDTO>> list(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(recurringService.list(user));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RecurringTransactionResponseDTO> toggle(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        return ResponseEntity.ok(recurringService.toggle(user, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        recurringService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
