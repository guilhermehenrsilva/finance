package com.finance.api.controller;

import com.finance.api.domain.account.AccountService;
import com.finance.api.domain.account.dto.AccountRequestDTO;
import com.finance.api.domain.account.dto.AccountResponseDTO;
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
    public ResponseEntity<List<AccountResponseDTO>> list(@AuthenticationPrincipal User user) {
        List<AccountResponseDTO> accounts = accountService.findUserAccounts(user);
        return ResponseEntity.ok(accounts);
    }
}