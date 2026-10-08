package com.finance.api.domain.transaction;

import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.category.Category;
import com.finance.api.domain.category.CategoryRepository;
import com.finance.api.domain.creditcard.CreditCardRepository;
import com.finance.api.domain.transaction.dto.RecurringTransactionRequestDTO;
import com.finance.api.domain.transaction.dto.RecurringTransactionResponseDTO;
import com.finance.api.domain.transaction.dto.TransactionRequestDTO;
import com.finance.api.domain.user.User;
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
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> recurrings = recurringRepository.findAllByActiveTrueAndNextDateLessThanEqual(today);

        for (RecurringTransaction recurring : recurrings) {
            
            // 1. Cria a transação real reutilizando a lógica que já existe
            TransactionRequestDTO dto = new TransactionRequestDTO(
                    recurring.getDescription(),
                    recurring.getAmount(),
                    recurring.getType(),
                    TransactionStatus.PENDING, // Lançado como pendente para não afetar saldo da conta bancária até ser pago de fato
                    recurring.getNextDate(),
                    recurring.getCategory().getId(),
                    recurring.getAccount() != null ? recurring.getAccount().getId() : null,
                    recurring.getCreditCard() != null ? recurring.getCreditCard().getId() : null,
                    1
            );

            transactionService.createTransaction(recurring.getUser(), dto);

            // 2. Calcula a próxima data com base na frequência
            if (recurring.getFrequency() == RecurringFrequency.MONTHLY) {
                recurring.setNextDate(recurring.getNextDate().plusMonths(1));
            } else if (recurring.getFrequency() == RecurringFrequency.WEEKLY) {
                recurring.setNextDate(recurring.getNextDate().plusWeeks(1));
            } else if (recurring.getFrequency() == RecurringFrequency.YEARLY) {
                recurring.setNextDate(recurring.getNextDate().plusYears(1));
            }

            // 3. Desativa se ultrapassou a data final estipulada
            if (recurring.getEndDate() != null && recurring.getNextDate().isAfter(recurring.getEndDate())) {
                recurring.setActive(false);
            }

            recurringRepository.save(recurring);
        }
    }

    @Transactional
    public RecurringTransactionResponseDTO create(User user, RecurringTransactionRequestDTO dto) {
        Category category = categoryRepository.findById(dto.categoryId())
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

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
                    .orElseThrow(() -> new RuntimeException("Conta não encontrada")));
        } else if (dto.creditCardId() != null) {
            recurring.setCreditCard(creditCardRepository.findById(dto.creditCardId())
                    .filter(c -> c.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new RuntimeException("Cartão não encontrado")));
        } else {
            throw new RuntimeException("Informe uma Conta ou Cartão de Crédito");
        }

        return new RecurringTransactionResponseDTO(recurringRepository.save(recurring));
    }
}