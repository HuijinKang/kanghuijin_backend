package com.example.remittanceservice.domain.transaction;

import java.time.ZonedDateTime;
import java.util.List;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findLatestByAccountIdAndType(long accountId, TransactionType type, int limit);

    List<Transaction> findLatestByAccountIdAndTypeBeforeId(long accountId, TransactionType type, long cursorExclusive, int limit);

    long sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
            long accountId,
            TransactionType type,
            TransactionStatus status,
            ZonedDateTime startInclusive,
            ZonedDateTime endExclusive
    );
}
