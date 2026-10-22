package com.finance.api.controller;

import com.finance.api.domain.account.AccountService;
import com.finance.api.domain.account.dto.AccountRequestDTO;
import com.finance.api.domain.account.dto.AccountResponseDTO;
import com.finance.api.domain.account.dto.AccountUpdateDTO;
import com.finance.api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDTO> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid AccountRequestDTO dto
    ) {
        AccountResponseDTO createdAccount = accountService.createAccount(user, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Boolean activeOnly
    ) {
        List<AccountResponseDTO> accounts = accountService.findUserAccounts(user, activeOnly);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getById(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        return ResponseEntity.ok(accountService.getAccountById(user, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> update(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @RequestBody @Valid AccountUpdateDTO dto
    ) {
        return ResponseEntity.ok(accountService.updateAccount(user, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        accountService.deleteAccount(user, id);
        return ResponseEntity.noContent().build();
    }
}