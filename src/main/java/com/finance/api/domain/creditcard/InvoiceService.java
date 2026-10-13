package com.finance.api.domain.creditcard;

import com.finance.api.domain.creditcard.dto.InvoiceResponseDTO;
import com.finance.api.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    public List<InvoiceResponseDTO> listUserInvoices(User user) {
        return invoiceRepository.findByCreditCardUserOrderByYearDescMonthDesc(user).stream()
                .map(InvoiceResponseDTO::new)
                .collect(Collectors.toList());
    }
}
