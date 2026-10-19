package com.finance.api.controller;

import com.finance.api.domain.transaction.TransactionService;
import com.finance.api.domain.transaction.dto.TransactionRequestDTO;
import com.finance.api.domain.transaction.dto.TransactionResponseDTO;
import com.finance.api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid TransactionRequestDTO dto
    ) {
        TransactionResponseDTO created = transactionService.createTransaction(user, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) com.finance.api.domain.transaction.TransactionType type,
            @RequestParam(required = false) com.finance.api.domain.transaction.TransactionStatus status,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String creditCardId,
            @RequestParam(required = false) String description
    ) {
        return ResponseEntity.ok(transactionService.searchUserTransactions(
                user, startDate, endDate, type, status, categoryId, accountId, creditCardId, description
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> update(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @RequestBody @Valid TransactionRequestDTO dto
    ) {
        return ResponseEntity.ok(transactionService.updateTransaction(user, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        transactionService.deleteTransaction(user, id);
        return ResponseEntity.noContent().build();
    }
}