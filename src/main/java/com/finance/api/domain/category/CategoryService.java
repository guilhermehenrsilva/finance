package com.finance.api.domain.category;

import com.finance.api.domain.category.dto.CategoryRequestDTO;
import com.finance.api.domain.category.dto.CategoryResponseDTO;
import com.finance.api.domain.transaction.TransactionType;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.BusinessException;
import com.finance.api.infra.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponseDTO createCategory(User user, CategoryRequestDTO dto) {
        Category category = new Category();
        category.setUser(user);
        category.setName(dto.name());
        category.setType(dto.type());
        category.setIcon(dto.icon());
        category.setColor(dto.color());

        if (dto.parentId() != null && !dto.parentId().isBlank()) {
            Category parent = categoryRepository.findById(dto.parentId())
                    .filter(c -> c.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria principal não encontrada"));
            category.setParentCategory(parent);
        }

        Category saved = categoryRepository.save(category);
        return new CategoryResponseDTO(saved);
    }

    public List<CategoryResponseDTO> listUserCategories(User user) {
        return listUserCategories(user, null, null);
    }

    public List<CategoryResponseDTO> listUserCategories(User user, TransactionType type, Boolean activeOnly) {
        List<Category> categories;

        if (type != null && activeOnly != null && activeOnly) {
            categories = categoryRepository.findByUserAndTypeAndActiveTrue(user, type);
        } else if (type != null) {
            categories = categoryRepository.findByUserAndType(user, type);
        } else if (activeOnly != null && activeOnly) {
            categories = categoryRepository.findByUserAndActiveTrue(user);
        } else {
            categories = categoryRepository.findByUser(user);
        }

        return categories.stream()
                .map(CategoryResponseDTO::new)
                .collect(Collectors.toList());
    }

    public CategoryResponseDTO getCategoryById(User user, String id) {
        Category category = findCategoryEntity(user, id);
        return new CategoryResponseDTO(category);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(User user, String id, CategoryRequestDTO dto) {
        Category category = findCategoryEntity(user, id);

        if (dto.parentId() != null && dto.parentId().equals(id)) {
            throw new BusinessException("Uma categoria não pode ser subcategoria de si mesma");
        }

        category.setName(dto.name());
        category.setType(dto.type());
        category.setIcon(dto.icon());
        category.setColor(dto.color());

        if (dto.parentId() != null && !dto.parentId().isBlank()) {
            Category parent = categoryRepository.findById(dto.parentId())
                    .filter(c -> c.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria principal não encontrada"));
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        Category updated = categoryRepository.save(category);
        return new CategoryResponseDTO(updated);
    }

    @Transactional
    public void deleteCategory(User user, String id) {
        Category category = findCategoryEntity(user, id);
        category.setActive(false);
        categoryRepository.save(category);
    }

    public Category findCategoryEntity(User user, String id) {
        return categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }
}