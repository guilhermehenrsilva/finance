package com.finance.api.domain.transaction;

import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.category.Category;
import com.finance.api.domain.category.CategoryRepository;
import com.finance.api.domain.creditcard.CreditCardRepository;
import com.finance.api.domain.transaction.dto.RecurringTransactionRequestDTO;
import com.finance.api.domain.transaction.dto.RecurringTransactionResponseDTO;
import com.finance.api.domain.transaction.dto.TransactionRequestDTO;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.BusinessException;
import com.finance.api.infra.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecurringTransactionService {

    @Autowired
    private RecurringTransactionRepository recurringRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    // Roda todos os dias às 02:00 da manhã
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void processRecurringTransactions() {
        processRecurringTransactions(LocalDate.now());
    }

    @Transactional
    void processRecurringTransactions(LocalDate today) {
        List<RecurringTransaction> recurrings = recurringRepository.findAllByActiveTrueAndNextDateLessThanEqual(today);

        for (RecurringTransaction recurring : recurrings) {
            while (recurring.getNextDate().isBefore(today) || recurring.getNextDate().isEqual(today)) {
                if (recurring.getEndDate() != null && recurring.getNextDate().isAfter(recurring.getEndDate())) {
                    recurring.setActive(false);
                    break;
                }

                TransactionRequestDTO dto = new TransactionRequestDTO(
                        recurring.getDescription(),
                        recurring.getAmount(),
                        recurring.getType(),
                        TransactionStatus.PENDING,
                        recurring.getNextDate(),
                        recurring.getCategory().getId(),
                        recurring.getAccount() != null ? recurring.getAccount().getId() : null,
                        recurring.getCreditCard() != null ? recurring.getCreditCard().getId() : null,
                        1
                );

                transactionService.createTransaction(recurring.getUser(), dto);
                recurring.setNextDate(nextDate(recurring.getNextDate(), recurring.getFrequency()));
            }

            if (recurring.getEndDate() != null && recurring.getNextDate().isAfter(recurring.getEndDate())) {
                recurring.setActive(false);
            }

            recurringRepository.save(recurring);
        }
    }

    private LocalDate nextDate(LocalDate date, RecurringFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case YEARLY -> date.plusYears(1);
        };
    }

    @Transactional
    public RecurringTransactionResponseDTO create(User user, RecurringTransactionRequestDTO dto) {
        validateRequest(dto);
        Category category = categoryRepository.findById(dto.categoryId())
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setUser(user);
        recurring.setCategory(category);
        recurring.setDescription(dto.description());
        recurring.setAmount(dto.amount());
        recurring.setType(dto.type());
        recurring.setFrequency(dto.frequency());
        recurring.setStartDate(dto.startDate());
        recurring.setEndDate(dto.endDate());
        recurring.setNextDate(dto.startDate()); // A primeira execução será na data de início configurada
        recurring.setActive(true);

        if (dto.accountId() != null) {
            recurring.setAccount(accountRepository.findById(dto.accountId())
                    .filter(a -> a.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada")));
        } else if (dto.creditCardId() != null) {
            recurring.setCreditCard(creditCardRepository.findById(dto.creditCardId())
                    .filter(c -> c.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Cartão não encontrado")));
        } else {
            throw new BusinessException("Informe uma Conta ou Cartão de Crédito");
        }

        return new RecurringTransactionResponseDTO(recurringRepository.save(recurring));
    }

    @Transactional(readOnly = true)
    public List<RecurringTransactionResponseDTO> list(User user) {
        return recurringRepository.findByUserOrderByNextDateAsc(user).stream()
                .map(RecurringTransactionResponseDTO::new)
                .toList();
    }

    @Transactional
    public RecurringTransactionResponseDTO toggle(User user, String id) {
        RecurringTransaction recurring = findUserRecurring(user, id);
        recurring.setActive(!Boolean.TRUE.equals(recurring.getActive()));
        return new RecurringTransactionResponseDTO(recurringRepository.save(recurring));
    }

    @Transactional
    public void delete(User user, String id) {
        recurringRepository.delete(findUserRecurring(user, id));
    }

    private RecurringTransaction findUserRecurring(User user, String id) {
        return recurringRepository.findById(id)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Transação recorrente não encontrada"));
    }

    private void validateRequest(RecurringTransactionRequestDTO dto) {
        if (dto.type() == TransactionType.TRANSFER) {
            throw new BusinessException("Transações recorrentes não podem ser transferências");
        }
        if (dto.endDate() != null && dto.endDate().isBefore(dto.startDate())) {
            throw new BusinessException("A data final não pode ser anterior à data de início");
        }
        boolean hasAccount = dto.accountId() != null && !dto.accountId().isBlank();
        boolean hasCard = dto.creditCardId() != null && !dto.creditCardId().isBlank();
        if (hasAccount == hasCard) {
            throw new BusinessException("Informe exatamente uma Conta ou Cartão de Crédito");
        }
    }
}