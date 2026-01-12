package com.example.remittanceservice.infrastructure.transaction;

import com.example.remittanceservice.domain.transaction.TransactionRepository;
import com.example.remittanceservice.domain.transaction.Transaction;
import com.example.remittanceservice.domain.transaction.TransactionStatus;
import com.example.remittanceservice.domain.transaction.TransactionType;
import com.example.remittanceservice.domain.transaction.TransferRoute;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository transactionJpaRepository;

    @Override
    public Transaction save(Transaction transaction) {
        return transactionJpaRepository.save(transaction);
    }

    @Override
    public Optional<Transaction> findByTransactionId(String transactionId) {
        return transactionJpaRepository.findByTransactionId(transactionId);
    }

    @Override
    public Optional<Transaction> findByTransferRouteAndIdempotencyKey(
            TransferRoute transferRoute,
            String idempotencyKey
    ) {
        return transactionJpaRepository.findByTransferRouteAndIdempotencyKey(transferRoute, idempotencyKey);
    }

    @Override
    public List<Transaction> findLatestByAccountIdAndType(long accountId, TransactionType type, int limit) {
        return transactionJpaRepository.findByAccountIdAndTypeOrderByIdDesc(
                accountId,
                type,
                PageRequest.of(0, limit)
        );
    }

    @Override
    public List<Transaction> findLatestByAccountIdAndTypeBeforeCursor(
            long accountId,
            TransactionType type,
            ZonedDateTime cursorCreatedAtExclusive,
            long cursorIdExclusive,
            int limit
    ) {
        return transactionJpaRepository.findByAccountIdAndTypeBeforeCursorOrderByCreatedAtDescIdDesc(
                accountId,
                type,
                cursorCreatedAtExclusive,
                cursorIdExclusive,
                PageRequest.of(0, limit)
        );
    }

    @Override
    public long sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
            long accountId,
            TransactionType type,
            TransactionStatus status,
            ZonedDateTime startInclusive,
            ZonedDateTime endExclusive
    ) {
        return transactionJpaRepository.sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
                accountId,
                type,
                status,
                startInclusive,
                endExclusive
        );
    }
}
