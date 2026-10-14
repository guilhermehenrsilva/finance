package com.finance.api.domain.creditcard;

import com.finance.api.domain.account.Account;
import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.creditcard.dto.InvoiceResponseDTO;
import com.finance.api.domain.creditcard.dto.InvoicePaymentRequestDTO;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.BusinessException;
import com.finance.api.infra.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private AccountRepository accountRepository;

    public List<InvoiceResponseDTO> listUserInvoices(User user) {
        return invoiceRepository.findByCreditCardUserOrderByYearDescMonthDesc(user).stream()
                .map(InvoiceResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvoiceResponseDTO pay(User user, String invoiceId, InvoicePaymentRequestDTO dto) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .filter(item -> item.getCreditCard().getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada"));

        Account account = accountRepository.findById(dto.accountId())
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Conta de pagamento não encontrada"));

        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount()).max(BigDecimal.ZERO);
        BigDecimal payment = dto.amount() == null ? remaining : dto.amount();

        if (payment.signum() <= 0) {
            throw new BusinessException("A fatura não possui valor pendente");
        }
        if (payment.compareTo(remaining) > 0) {
            throw new BusinessException("O pagamento não pode exceder o valor pendente da fatura");
        }

        BigDecimal accountBalance = account.getCurrentBalance() == null
                ? BigDecimal.ZERO
                : account.getCurrentBalance();
        if (accountBalance.compareTo(payment) < 0) {
            throw new BusinessException("Saldo insuficiente na conta de pagamento");
        }

        account.setCurrentBalance(accountBalance.subtract(payment));
        invoice.setPaidAmount(invoice.getPaidAmount().add(payment));
        if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setPaidAmount(invoice.getTotalAmount());
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.CLOSED);
        }

        CreditCard card = invoice.getCreditCard();
        BigDecimal creditLimit = card.getCreditLimit() == null ? BigDecimal.ZERO : card.getCreditLimit();
        BigDecimal availableLimit = card.getAvailableLimit() == null ? BigDecimal.ZERO : card.getAvailableLimit();
        card.setAvailableLimit(availableLimit.add(payment).min(creditLimit));

        accountRepository.save(account);
        invoiceRepository.save(invoice);
        return new InvoiceResponseDTO(invoice);
    }
}
