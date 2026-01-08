package com.example.remittanceservice.domain.transaction;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Optional<Transaction> findById(long id);

    Transaction save(Transaction transaction);

    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    long sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
            long accountId,
            TransactionType type,
            TransactionStatus status,
            ZonedDateTime startInclusive,
            ZonedDateTime endExclusive
    );
}
