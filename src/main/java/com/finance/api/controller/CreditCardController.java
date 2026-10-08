package com.finance.api.controller;

import com.finance.api.domain.creditcard.CreditCardService;
import com.finance.api.domain.creditcard.dto.CreditCardRequestDTO;
import com.finance.api.domain.creditcard.dto.CreditCardResponseDTO;
import com.finance.api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credit-cards")
public class CreditCardController {

    @Autowired
    private CreditCardService creditCardService;

    @PostMapping
    public ResponseEntity<CreditCardResponseDTO> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CreditCardRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(creditCardService.createCreditCard(user, dto));
    }

    @GetMapping
    public ResponseEntity<List<CreditCardResponseDTO>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(creditCardService.listUserCreditCards(user));
    }
}