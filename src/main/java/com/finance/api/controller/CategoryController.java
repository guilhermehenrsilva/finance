package com.finance.api.controller;

import com.finance.api.domain.category.CategoryService;
import com.finance.api.domain.category.dto.CategoryRequestDTO;
import com.finance.api.domain.category.dto.CategoryResponseDTO;
import com.finance.api.domain.transaction.TransactionType;
import com.finance.api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CategoryRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(user, dto));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Boolean activeOnly
    ) {
        return ResponseEntity.ok(categoryService.listUserCategories(user, type, activeOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getById(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        return ResponseEntity.ok(categoryService.getCategoryById(user, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> update(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @RequestBody @Valid CategoryRequestDTO dto
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(user, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        categoryService.deleteCategory(user, id);
        return ResponseEntity.noContent().build();
    }
}