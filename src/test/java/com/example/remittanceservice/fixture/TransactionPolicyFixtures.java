package com.example.remittanceservice.fixture;

import com.example.remittanceservice.domain.transactionpolicy.TransactionPolicy;
import com.example.remittanceservice.domain.transactionpolicy.TransactionPolicyType;
import com.example.remittanceservice.infrastructure.transactionpolicy.TransactionPolicyJpaRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionPolicyFixtures {

    /**
     * DB에 기본 정책 설정 (출금 1,000,000원, 이체 3,000,000원, 수수료 1%)
     */
    public static void setupDefaultPolicy(TransactionPolicyJpaRepository repository) {
        TransactionPolicy policy = repository.findByPolicyType(TransactionPolicyType.DEFAULT)
                .orElseGet(() -> repository.save(
                        TransactionPolicy.of(
                                TransactionPolicyType.DEFAULT,
                                1_000_000L,
                                3_000_000L,
                                100
                        )
                ));
        
        policy.update(1_000_000L, 3_000_000L, 100);
        repository.save(policy);
    }
}
