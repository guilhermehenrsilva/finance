package com.finance.api.domain.category;

import com.finance.api.domain.category.dto.CategoryRequestDTO;
import com.finance.api.domain.category.dto.CategoryResponseDTO;
import com.finance.api.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryResponseDTO createCategory(User user, CategoryRequestDTO dto) {
        Category category = new Category();
        category.setUser(user);
        category.setName(dto.name());
        category.setType(dto.type());
        category.setIcon(dto.icon());
        category.setColor(dto.color());

        // Se o utilizador enviou um parentId, validamos se a categoria pai existe e pertence a ele
        if (dto.parentId() != null && !dto.parentId().isBlank()) {
            Category parent = categoryRepository.findById(dto.parentId())
                    .filter(c -> c.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new RuntimeException("Categoria principal não encontrada"));
            category.setParentCategory(parent);
        }

        Category saved = categoryRepository.save(category);
        return new CategoryResponseDTO(saved);
    }

    public List<CategoryResponseDTO> listUserCategories(User user) {
        return categoryRepository.findByUser(user).stream()
                .map(CategoryResponseDTO::new)
                .collect(Collectors.toList());
    }
}