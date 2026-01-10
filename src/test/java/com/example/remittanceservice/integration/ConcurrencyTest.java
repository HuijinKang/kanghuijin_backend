package com.example.remittanceservice.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.remittanceservice.TestcontainersConfiguration;
import com.example.remittanceservice.application.command.DepositCommand;
import com.example.remittanceservice.application.command.TransferCommand;
import com.example.remittanceservice.application.command.WithdrawCommand;
import com.example.remittanceservice.application.facade.TransactionFacade;
import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.domain.policy.PolicyConfig;
import com.example.remittanceservice.domain.policy.PolicyType;
import com.example.remittanceservice.infrastructure.account.AccountJpaRepository;
import com.example.remittanceservice.infrastructure.policy.PolicyConfigJpaRepository;
import com.example.remittanceservice.infrastructure.transaction.TransactionJpaRepository;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ConcurrencyTest {

    @Autowired
    private TransactionFacade transactionFacade;

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    @Autowired
    private TransactionJpaRepository transactionJpaRepository;

    @Autowired
    private PolicyConfigJpaRepository policyConfigJpaRepository;

    @BeforeEach
    void setUp() {
        transactionJpaRepository.deleteAll();
        accountJpaRepository.deleteAll();

        // 정책 초기화 - 일 한도를 충분히 높게 설정하여 한도 초과 방지
        PolicyConfig policy = policyConfigJpaRepository.findByPolicyType(PolicyType.DEFAULT)
                .orElseGet(() -> policyConfigJpaRepository.save(
                        PolicyConfig.of(PolicyType.DEFAULT, 10_000_000L, 10_000_000L, 100)));

        policy.update(10_000_000L, 10_000_000L, 100);
        policyConfigJpaRepository.save(policy);
    }

    @Test
    @DisplayName("동시성: 100개 스레드가 같은 계좌에 동시 입금 -> 잔액 일관성 유지")
    void concurrent_deposit_maintains_balance_consistency() throws Exception {
        // given
        Account account = accountJpaRepository.save(Account.create("100000000001", "테스트"));

        int threadCount = 100;
        long depositAmount = 1000L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When - 100개 스레드에서 동시 입금
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    transactionFacade.deposit(DepositCommand.of(account.getId(), depositAmount));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then - 최종 잔액 = 100 * 1000 = 100,000
        Account result = accountJpaRepository.findById(account.getId()).orElseThrow();
        assertThat(result.getBalance()).isEqualTo(threadCount * depositAmount);
    }

    @Test
    @DisplayName("동시성: 100개 스레드가 같은 계좌에서 동시 출금 -> 잔액 일관성 유지")
    void concurrent_withdraw_maintains_balance_consistency() throws Exception {
        // given - 초기 잔액 200,000원
        Account account = accountJpaRepository.save(Account.create("100000000002", "테스트"));
        account.deposit(200_000L);
        accountJpaRepository.save(account);

        int threadCount = 100;
        long withdrawAmount = 1000L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when - 100개 스레드에서 동시 출금
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    transactionFacade.withdraw(WithdrawCommand.of(account.getId(), withdrawAmount));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        // then - 최종 잔액 = 200,000 - (100 * 1,000) = 100,000
        Account result = accountJpaRepository.findById(account.getId()).orElseThrow();
        assertThat(result.getBalance()).isEqualTo(100_000L);
    }

    @Test
    @DisplayName("동시성: A↔B 왕복 이체 -> 데드락 없이 완료, 수수료만 차감")
    void concurrent_bidirectional_transfer_avoids_deadlock() throws Exception {
        // given - 두 계좌 각각 100,000원
        Account accountA = accountJpaRepository.save(Account.create("100000000003", "A"));
        Account accountB = accountJpaRepository.save(Account.create("100000000004", "B"));
        accountA.deposit(100_000L);
        accountB.deposit(100_000L);
        accountJpaRepository.saveAll(List.of(accountA, accountB));

        int iterations = 50;
        long transferAmount = 500L;
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2 * iterations);

        // when - A→B 50번, B→A 50번 동시 실행
        for (int i = 0; i < iterations; i++) {
            // A → B
            executorService.submit(() -> {
                try {
                    transactionFacade.transfer(TransferCommand.of(
                            accountA.getAccountNumber(),
                            accountB.getAccountNumber(),
                            transferAmount
                    ));
                } finally {
                    latch.countDown();
                }
            });

            // B → A
            executorService.submit(() -> {
                try {
                    transactionFacade.transfer(TransferCommand.of(
                            accountB.getAccountNumber(),
                            accountA.getAccountNumber(),
                            transferAmount
                    ));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executorService.shutdown();

        // then - 데드락 없이 완료, 총 잔액 = 200,000 - (100건 * 수수료 5원)
        Account resultA = accountJpaRepository.findById(accountA.getId()).orElseThrow();
        Account resultB = accountJpaRepository.findById(accountB.getId()).orElseThrow();

        long totalBalance = resultA.getBalance() + resultB.getBalance();
        long expectedFee = 100 * 5; // 100건 * 1% of 500 = 5원
        assertThat(totalBalance).isEqualTo(200_000L - expectedFee);
    }
}
