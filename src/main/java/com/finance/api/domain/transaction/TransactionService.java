package com.finance.api.domain.transaction;

import com.finance.api.domain.account.Account;
import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.transaction.dto.TransactionRequestDTO;
import com.finance.api.domain.transaction.dto.TransactionResponseDTO;
import com.finance.api.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional // Garante que ou guarda a transação e atualiza o saldo, ou falha tudo e dá rollback
    public TransactionResponseDTO createTransaction(User user, TransactionRequestDTO dto) {
        // 1. Busca a conta e valida se pertence ao utilizador
        Account account = accountRepository.findById(dto.accountId())
                .filter(acc -> acc.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Conta não encontrada ou não pertence ao usuário"));

        // 2. Cria a transação
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccount(account);
        transaction.setDescription(dto.description());
        transaction.setAmount(dto.amount());
        transaction.setType(dto.type());
        transaction.setStatus(dto.status());
        transaction.setDate(dto.date());

        // 3. Regra de Negócio: Se a transação já estiver PAGA, atualiza o saldo da conta
        if (transaction.getStatus() == TransactionStatus.PAID) {
            if (transaction.getType() == TransactionType.INCOME) {
                account.setCurrentBalance(account.getCurrentBalance().add(transaction.getAmount()));
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                account.setCurrentBalance(account.getCurrentBalance().subtract(transaction.getAmount()));
            }
            accountRepository.save(account);
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        return new TransactionResponseDTO(savedTransaction);
    }

    public List<TransactionResponseDTO> listUserTransactions(User user) {
        return transactionRepository.findByUserOrderByDateDesc(user).stream()
                .map(TransactionResponseDTO::new)
                .collect(Collectors.toList());
    }
}