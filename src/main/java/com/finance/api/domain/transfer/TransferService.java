package com.finance.api.domain.transfer;

import com.finance.api.domain.account.Account;
import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.transfer.dto.TransferRequestDTO;
import com.finance.api.domain.transfer.dto.TransferResponseDTO;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.BusinessException;
import com.finance.api.infra.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;

    public TransferService(
            TransferRepository transferRepository,
            AccountRepository accountRepository
    ) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public TransferResponseDTO create(User user, TransferRequestDTO dto) {
        Account source = findUserAccount(user, dto.sourceAccountId(), "Conta de origem não encontrada");
        Account destination = findUserAccount(user, dto.destinationAccountId(), "Conta de destino não encontrada");

        if (source.getId().equals(destination.getId())) {
            throw new BusinessException("A conta de origem e destino devem ser diferentes");
        }

        BigDecimal sourceBalance = balanceOf(source);
        if (sourceBalance.compareTo(dto.amount()) < 0) {
            throw new BusinessException("Saldo insuficiente na conta de origem");
        }

        source.setCurrentBalance(sourceBalance.subtract(dto.amount()));
        destination.setCurrentBalance(balanceOf(destination).add(dto.amount()));
        accountRepository.save(source);
        accountRepository.save(destination);

        Transfer transfer = new Transfer();
        transfer.setUser(user);
        transfer.setSourceAccount(source);
        transfer.setDestinationAccount(destination);
        transfer.setAmount(dto.amount());
        transfer.setDate(dto.date());
        transfer.setDescription(dto.description());

        return new TransferResponseDTO(transferRepository.save(transfer));
    }

    @Transactional(readOnly = true)
    public List<TransferResponseDTO> list(User user) {
        return transferRepository.findByUserOrderByDateDesc(user).stream()
                .map(TransferResponseDTO::new)
                .toList();
    }

    @Transactional
    public void delete(User user, String transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Transferência não encontrada"));

        Account source = transfer.getSourceAccount();
        Account destination = transfer.getDestinationAccount();
        BigDecimal destinationBalance = balanceOf(destination);

        if (destinationBalance.compareTo(transfer.getAmount()) < 0) {
            throw new BusinessException(
                    "Não é possível estornar a transferência: saldo insuficiente na conta de destino"
            );
        }

        source.setCurrentBalance(balanceOf(source).add(transfer.getAmount()));
        destination.setCurrentBalance(destinationBalance.subtract(transfer.getAmount()));
        accountRepository.save(source);
        accountRepository.save(destination);
        transferRepository.delete(transfer);
    }

    private Account findUserAccount(User user, String accountId, String message) {
        return accountRepository.findById(accountId)
                .filter(account -> account.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(message));
    }

    private BigDecimal balanceOf(Account account) {
        return account.getCurrentBalance() == null
                ? BigDecimal.ZERO
                : account.getCurrentBalance();
    }
}
