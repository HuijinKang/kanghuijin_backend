package com.example.remittanceservice.fixture;

import com.example.remittanceservice.infrastructure.account.AccountJpaRepository;
import com.example.remittanceservice.infrastructure.transaction.TransactionJpaRepository;
import com.example.remittanceservice.infrastructure.transactionpolicy.TransactionPolicyJpaRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseFixtures {

    /**
     * 전체 테스트 환경 초기화 (DB 정리 + 기본 정책 설정)
     */
    public static void setupTestEnvironment(
            AccountJpaRepository accountRepository,
            TransactionJpaRepository transactionRepository,
            TransactionPolicyJpaRepository policyRepository
    ) {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        TransactionPolicyFixtures.setupDefaultPolicy(policyRepository);
    }
}
