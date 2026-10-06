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
    public ResponseEntity<List<TransactionResponseDTO>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.listUserTransactions(user));
    }
}