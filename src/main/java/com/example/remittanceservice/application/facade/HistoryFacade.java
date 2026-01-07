package com.example.remittanceservice.application.facade;

import com.example.remittanceservice.application.service.HistoryService;
import com.example.remittanceservice.domain.transaction.Transaction;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HistoryFacade {

    private final HistoryService historyService;

    public List<Transaction> getTransactions(long accountId) {
        return historyService.getTransactions(accountId);
    }
}
