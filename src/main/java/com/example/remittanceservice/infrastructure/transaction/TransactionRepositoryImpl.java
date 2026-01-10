package com.example.remittanceservice.infrastructure.transaction;

import com.example.remittanceservice.domain.transaction.TransactionRepository;
import com.example.remittanceservice.domain.transaction.Transaction;
import com.example.remittanceservice.domain.transaction.TransactionStatus;
import com.example.remittanceservice.domain.transaction.TransactionType;
import java.time.ZonedDateTime;
import java.util.List;
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
    public List<Transaction> findLatestByAccountIdAndType(long accountId, TransactionType type, int limit) {
        return transactionJpaRepository.findByAccountIdAndTypeOrderByIdDesc(
                accountId,
                type,
                PageRequest.of(0, limit)
        );
    }

    @Override
    public List<Transaction> findLatestByAccountIdAndTypeBeforeId(long accountId, TransactionType type, long cursorExclusive, int limit) {
        return transactionJpaRepository.findByAccountIdAndTypeAndIdLessThanOrderByIdDesc(
                accountId,
                type,
                cursorExclusive,
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
