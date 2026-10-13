package com.finance.api.controller;

import com.finance.api.domain.transfer.TransferService;
import com.finance.api.domain.transfer.dto.TransferRequestDTO;
import com.finance.api.domain.transfer.dto.TransferResponseDTO;
import com.finance.api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponseDTO> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid TransferRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferService.create(user, dto));
    }

    @GetMapping
    public ResponseEntity<List<TransferResponseDTO>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transferService.list(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        transferService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
