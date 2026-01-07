package com.example.remittanceservice.infrastructure.persistence;

import com.example.remittanceservice.application.port.out.TransactionRepository;
import com.example.remittanceservice.domain.transaction.Transaction;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {

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
}
