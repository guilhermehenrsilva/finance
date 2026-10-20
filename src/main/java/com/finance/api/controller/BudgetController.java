package com.finance.api.controller;

import com.finance.api.domain.budget.BudgetService;
import com.finance.api.domain.budget.dto.BudgetRequestDTO;
import com.finance.api.domain.budget.dto.BudgetResponseDTO;
import com.finance.api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponseDTO> create(@AuthenticationPrincipal User user, @RequestBody @Valid BudgetRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.create(user, dto));
    }

    @GetMapping
    public List<BudgetResponseDTO> list(@AuthenticationPrincipal User user,
                                        @RequestParam(required = false) Integer month,
                                        @RequestParam(required = false) Integer year) {
        return budgetService.list(user, month, year);
    }

    @PutMapping("/{id}")
    public BudgetResponseDTO update(@AuthenticationPrincipal User user, @PathVariable String id, @RequestBody @Valid BudgetRequestDTO dto) {
        return budgetService.update(user, id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable String id) {
        budgetService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
