package com.finance.api.domain.budget;

import com.finance.api.domain.category.Category;
import com.finance.api.domain.category.CategoryRepository;
import com.finance.api.domain.transaction.TransactionRepository;
import com.finance.api.domain.user.User;
import com.finance.api.domain.budget.dto.BudgetRequestDTO;
import com.finance.api.domain.budget.dto.BudgetResponseDTO;
import com.finance.api.infra.exception.BusinessException;
import com.finance.api.infra.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository, CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public BudgetResponseDTO create(User user, BudgetRequestDTO dto) {
        if (budgetRepository.existsByUserAndCategoryIdAndMonthAndYear(user, dto.categoryId(), dto.month(), dto.year())) {
            throw new BusinessException("Já existe um orçamento para essa categoria no período");
        }
        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category(user, dto.categoryId()));
        budget.setMonth(dto.month());
        budget.setYear(dto.year());
        budget.setAmount(dto.amount());
        return response(budgetRepository.save(budget));
    }

    @Transactional(readOnly = true)
    public List<BudgetResponseDTO> list(User user, Integer month, Integer year) {
        LocalDate today = LocalDate.now();
        int selectedMonth = month == null ? today.getMonthValue() : month;
        int selectedYear = year == null ? today.getYear() : year;
        return budgetRepository.findByUserAndYearAndMonthOrderByCategory_NameAsc(user, selectedYear, selectedMonth)
                .stream().map(this::response).toList();
    }

    @Transactional
    public BudgetResponseDTO update(User user, String id, BudgetRequestDTO dto) {
        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Orçamento não encontrado"));
        budget.setCategory(category(user, dto.categoryId()));
        budget.setMonth(dto.month());
        budget.setYear(dto.year());
        budget.setAmount(dto.amount());
        return response(budgetRepository.save(budget));
    }

    @Transactional
    public void delete(User user, String id) {
        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Orçamento não encontrado"));
        budgetRepository.delete(budget);
    }

    private Category category(User user, String id) {
        Category category = categoryRepository.findById(id)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        if (category.getType() != com.finance.api.domain.transaction.TransactionType.EXPENSE) {
            throw new BusinessException("Orçamentos devem usar categorias de despesa");
        }
        return category;
    }

    private BudgetResponseDTO response(Budget budget) {
        LocalDate start = LocalDate.of(budget.getYear(), budget.getMonth(), 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        BigDecimal spent = transactionRepository.sumPaidExpensesByUserAndCategoryAndDateBetween(
                budget.getUser(), budget.getCategory(), start, end,
                com.finance.api.domain.transaction.TransactionType.EXPENSE,
                com.finance.api.domain.transaction.TransactionStatus.PAID);
        return new BudgetResponseDTO(budget, spent == null ? BigDecimal.ZERO : spent);
    }
}
