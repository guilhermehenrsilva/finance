package com.finance.api.controller;

import com.finance.api.domain.tag.TagService;
import com.finance.api.domain.tag.dto.TagRequestDTO;
import com.finance.api.domain.tag.dto.TagResponseDTO;
import com.finance.api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @PostMapping
    public ResponseEntity<TagResponseDTO> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid TagRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(user, dto));
    }

    @GetMapping
    public ResponseEntity<List<TagResponseDTO>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(tagService.listTags(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDTO> getById(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        return ResponseEntity.ok(tagService.getTagById(user, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDTO> update(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @RequestBody @Valid TagRequestDTO dto
    ) {
        return ResponseEntity.ok(tagService.updateTag(user, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        tagService.deleteTag(user, id);
        return ResponseEntity.noContent().build();
    }
}
