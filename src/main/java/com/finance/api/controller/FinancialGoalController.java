package com.finance.api.controller;

import com.finance.api.domain.goal.FinancialGoalService;
import com.finance.api.domain.goal.dto.*;
import com.finance.api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class FinancialGoalController {
    private final FinancialGoalService service;

    public FinancialGoalController(FinancialGoalService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FinancialGoalResponseDTO> create(@AuthenticationPrincipal User user, @RequestBody @Valid FinancialGoalRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(user, dto));
    }

    @GetMapping
    public List<FinancialGoalResponseDTO> list(@AuthenticationPrincipal User user) {
        return service.list(user);
    }

    @PostMapping("/{id}/contributions")
    public FinancialGoalResponseDTO contribute(@AuthenticationPrincipal User user, @PathVariable String id, @RequestBody @Valid GoalContributionRequestDTO dto) {
        return service.contribute(user, id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable String id) {
        service.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
