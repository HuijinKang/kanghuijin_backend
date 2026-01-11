package com.example.remittanceservice.infrastructure.transactionpolicy;

import com.example.remittanceservice.domain.transactionpolicy.TransactionPolicy;
import com.example.remittanceservice.domain.transactionpolicy.TransactionPolicyType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionPolicyJpaRepository extends JpaRepository<TransactionPolicy, Long> {
    Optional<TransactionPolicy> findByPolicyType(TransactionPolicyType policyType);
}
