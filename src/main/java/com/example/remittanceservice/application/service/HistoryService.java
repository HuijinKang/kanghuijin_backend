package com.example.remittanceservice.application.service;

import com.example.remittanceservice.domain.transaction.TransactionRepository;
import com.example.remittanceservice.domain.transaction.Transaction;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> getTransactions(long accountId) {
        throw new UnsupportedOperationException("NOT_IMPLEMENTED");
    }
}
