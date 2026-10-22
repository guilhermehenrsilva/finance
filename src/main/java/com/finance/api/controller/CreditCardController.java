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
    public ResponseEntity<List<CreditCardResponseDTO>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Boolean activeOnly
    ) {
        return ResponseEntity.ok(creditCardService.listUserCreditCards(user, activeOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCardResponseDTO> getById(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        return ResponseEntity.ok(creditCardService.getCreditCardById(user, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreditCardResponseDTO> update(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @RequestBody @Valid CreditCardRequestDTO dto
    ) {
        return ResponseEntity.ok(creditCardService.updateCreditCard(user, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        creditCardService.deleteCreditCard(user, id);
        return ResponseEntity.noContent().build();
    }
}