package com.example.remittanceservice.infrastructure.transaction;

import com.example.remittanceservice.domain.transaction.TransactionRepository;
import com.example.remittanceservice.domain.transaction.Transaction;
import com.example.remittanceservice.domain.transaction.TransactionStatus;
import com.example.remittanceservice.domain.transaction.TransactionType;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository transactionJpaRepository;

    @Override
    public Optional<Transaction> findById(long id) {
        return transactionJpaRepository.findById(id);
    }

    @Override
    public Transaction save(Transaction transaction) {
        return transactionJpaRepository.save(transaction);
    }

    @Override
    public List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId) {
        return transactionJpaRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
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
                accountId, type, status, startInclusive, endExclusive
        );
    }
}
