package com.finance.api.domain.transaction;

import com.finance.api.domain.account.Account;
import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.category.Category;
import com.finance.api.domain.category.CategoryRepository;
import com.finance.api.domain.creditcard.CreditCard;
import com.finance.api.domain.creditcard.CreditCardRepository;
import com.finance.api.domain.creditcard.Invoice;
import com.finance.api.domain.creditcard.InvoiceRepository;
import com.finance.api.domain.transaction.dto.TransactionRequestDTO;
import com.finance.api.domain.transaction.dto.TransactionResponseDTO;
import com.finance.api.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Transactional
    public TransactionResponseDTO createTransaction(User user, TransactionRequestDTO dto) {
        
        Category category = categoryRepository.findById(dto.categoryId())
                .filter(cat -> cat.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        int installments = (dto.totalInstallments() != null && dto.totalInstallments() > 1) ? dto.totalInstallments() : 1;
        BigDecimal installmentAmount = dto.amount().divide(new BigDecimal(installments), 2, RoundingMode.HALF_UP);
        String installmentGroupId = installments > 1 ? UUID.randomUUID().toString() : null;

        Transaction lastSavedTransaction = null;

        if (dto.creditCardId() != null && !dto.creditCardId().isBlank()) {
            if (dto.type() != TransactionType.EXPENSE) {
                throw new RuntimeException("Cartões de crédito apenas aceitam despesas");
            }

            CreditCard card = creditCardRepository.findById(dto.creditCardId())
                    .filter(c -> c.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));

            if (card.getAvailableLimit().compareTo(dto.amount()) < 0) {
                throw new RuntimeException("Limite insuficiente no cartão de crédito");
            }

            card.setAvailableLimit(card.getAvailableLimit().subtract(dto.amount()));
            creditCardRepository.save(card);

            for (int i = 1; i <= installments; i++) {
                int invoiceMonth = dto.date().getMonthValue();
                int invoiceYear = dto.date().getYear();
                
                int monthsToAdd = (i - 1) + (dto.date().getDayOfMonth() >= card.getClosingDay() ? 1 : 0);
                
                invoiceMonth += monthsToAdd;
                while (invoiceMonth > 12) {
                    invoiceMonth -= 12;
                    invoiceYear++;
                }

                final int finalInvoiceMonth = invoiceMonth;
                final int finalInvoiceYear = invoiceYear;

                Invoice invoice = invoiceRepository.findByCreditCardAndMonthAndYear(card, finalInvoiceMonth, finalInvoiceYear)
                        .orElseGet(() -> {
                            Invoice newInvoice = new Invoice();
                            newInvoice.setCreditCard(card);
                            newInvoice.setMonth(finalInvoiceMonth);
                            newInvoice.setYear(finalInvoiceYear);
                            return invoiceRepository.save(newInvoice);
                        });

                invoice.setTotalAmount(invoice.getTotalAmount().add(installmentAmount));
                invoiceRepository.save(invoice);

                Transaction transaction = new Transaction();
                transaction.setUser(user);
                transaction.setCategory(category);
                transaction.setDescription(installments > 1 ? dto.description() + " (" + i + "/" + installments + ")" : dto.description());
                transaction.setAmount(installmentAmount);
                transaction.setType(dto.type());
                transaction.setStatus(dto.status());
                transaction.setDate(dto.date().plusMonths(i - 1));
                transaction.setCreditCard(card);
                transaction.setInvoice(invoice);
                transaction.setInstallmentGroupId(installmentGroupId);
                transaction.setInstallmentNumber(i);
                transaction.setTotalInstallments(installments);

                lastSavedTransaction = transactionRepository.save(transaction);
            }

        } else if (dto.accountId() != null && !dto.accountId().isBlank()) {
            Account account = accountRepository.findById(dto.accountId())
                    .filter(acc -> acc.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setCategory(category);
            transaction.setDescription(dto.description());
            transaction.setAmount(dto.amount());
            transaction.setType(dto.type());
            transaction.setStatus(dto.status());
            transaction.setDate(dto.date());
            transaction.setAccount(account);
            transaction.setInstallmentGroupId(installmentGroupId);
            transaction.setInstallmentNumber(1);
            transaction.setTotalInstallments(1);

            if (transaction.getStatus() == TransactionStatus.PAID) {
                if (transaction.getType() == TransactionType.INCOME) {
                    account.setCurrentBalance(account.getCurrentBalance().add(transaction.getAmount()));
                } else if (transaction.getType() == TransactionType.EXPENSE) {
                    account.setCurrentBalance(account.getCurrentBalance().subtract(transaction.getAmount()));
                }
                accountRepository.save(account);
            }

            lastSavedTransaction = transactionRepository.save(transaction);
        } else {
            throw new RuntimeException("É necessário informar uma Conta ou um Cartão de Crédito");
        }

        return new TransactionResponseDTO(lastSavedTransaction);
    }

    public List<TransactionResponseDTO> listUserTransactions(User user) {
        return transactionRepository.findByUserOrderByDateDesc(user).stream()
                .map(TransactionResponseDTO::new)
                .collect(Collectors.toList());
    }
}