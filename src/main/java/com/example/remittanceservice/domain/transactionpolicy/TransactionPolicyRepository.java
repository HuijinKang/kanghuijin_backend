package com.example.remittanceservice.domain.transactionpolicy;

import java.util.Optional;

public interface TransactionPolicyRepository {

    Optional<TransactionPolicy> findByPolicyType(TransactionPolicyType policyType);

    TransactionPolicy save(TransactionPolicy policy);
}
