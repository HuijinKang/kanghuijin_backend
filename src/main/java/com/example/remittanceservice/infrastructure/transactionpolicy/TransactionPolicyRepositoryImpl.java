package com.example.remittanceservice.infrastructure.transactionpolicy;

import com.example.remittanceservice.domain.transactionpolicy.TransactionPolicyRepository;
import com.example.remittanceservice.domain.transactionpolicy.TransactionPolicy;
import com.example.remittanceservice.domain.transactionpolicy.TransactionPolicyType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TransactionPolicyRepositoryImpl implements TransactionPolicyRepository {

    private final TransactionPolicyJpaRepository transactionPolicyJpaRepository;

    @Override
    public Optional<TransactionPolicy> findByPolicyType(TransactionPolicyType policyType) {
        return transactionPolicyJpaRepository.findByPolicyType(policyType);
    }

    @Override
    public TransactionPolicy save(TransactionPolicy policy) {
        return transactionPolicyJpaRepository.save(policy);
    }
}
