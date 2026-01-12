package com.example.remittanceservice.domain.transaction;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findByTransactionId(String transactionId);

    Optional<Transaction> findByRequestClientAndIdempotencyKey(
            TransactionRequestClient requestClient,
            String idempotencyKey
    );

    List<Transaction> findLatestByAccountIdAndType(long accountId, TransactionType type, int limit);

    List<Transaction> findLatestByAccountIdAndTypeBeforeCursor(
            long accountId,
            TransactionType type,
            ZonedDateTime cursorCreatedAtExclusive,
            long cursorIdExclusive,
            int limit
    );

    long sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
            long accountId,
            TransactionType type,
            TransactionStatus status,
            ZonedDateTime startInclusive,
            ZonedDateTime endExclusive
    );
}
