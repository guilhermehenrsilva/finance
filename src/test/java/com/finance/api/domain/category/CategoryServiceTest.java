package com.finance.api.domain.category;

import com.finance.api.domain.category.dto.CategoryRequestDTO;
import com.finance.api.domain.category.dto.CategoryResponseDTO;
import com.finance.api.domain.transaction.TransactionType;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldCreateCategory() {
        User user = new User("User", "user@test.com", "pass", "BRL");
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryRequestDTO dto = new CategoryRequestDTO("Alimentação", TransactionType.EXPENSE, "utensils", "#FF0000", null);
        CategoryResponseDTO response = categoryService.createCategory(user, dto);

        assertEquals("Alimentação", response.name());
        assertEquals(TransactionType.EXPENSE, response.type());
    }

    @Test
    void shouldRejectSelfSubcategory() {
        User user = new User("User", "user@test.com", "pass", "BRL");
        Category category = new Category();
        category.setId("cat-1");
        category.setUser(user);

        when(categoryRepository.findByIdAndUser("cat-1", user)).thenReturn(Optional.of(category));

        CategoryRequestDTO dto = new CategoryRequestDTO("Alimentação", TransactionType.EXPENSE, "utensils", "#FF0000", "cat-1");

        assertThrows(BusinessException.class, () -> categoryService.updateCategory(user, "cat-1", dto));
    }

    @Test
    void shouldSoftDeleteCategory() {
        User user = new User("User", "user@test.com", "pass", "BRL");
        Category category = new Category();
        category.setId("cat-1");
        category.setUser(user);
        category.setActive(true);

        when(categoryRepository.findByIdAndUser("cat-1", user)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        categoryService.deleteCategory(user, "cat-1");

        assertFalse(category.getActive());
        verify(categoryRepository).save(category);
    }
}
