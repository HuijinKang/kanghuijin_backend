package com.example.remittanceservice.application.port.out;

import com.example.remittanceservice.domain.transaction.Transaction;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Optional<Transaction> findById(long id);

    Transaction save(Transaction transaction);

    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
