package com.example.remittanceservice.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.remittanceservice.TestcontainersConfiguration;
import com.example.remittanceservice.application.command.DepositCommand;
import com.example.remittanceservice.application.command.TransferCommand;
import com.example.remittanceservice.application.facade.TransactionFacade;
import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.fixture.AccountFixtures;
import com.example.remittanceservice.fixture.DatabaseFixtures;
import com.example.remittanceservice.infrastructure.account.AccountJpaRepository;
import com.example.remittanceservice.infrastructure.transactionpolicy.TransactionPolicyJpaRepository;
import com.example.remittanceservice.infrastructure.transaction.TransactionJpaRepository;
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
    private TransactionPolicyJpaRepository transactionPolicyJpaRepository;

    @BeforeEach
    void setUp() {
        DatabaseFixtures.setupTestEnvironment(
                accountJpaRepository,
                transactionJpaRepository,
                transactionPolicyJpaRepository
        );
    }

    @Test
    @DisplayName("동시성: 100개 스레드가 같은 계좌에 동시 입금 -> 잔액 일관성 유지")
    void concurrent_deposit_maintains_balance_consistency() throws Exception {
        // given
        Account account = AccountFixtures.createAndSave(accountJpaRepository, "테스트");

        int threadCount = 100;
        long depositAmount = 1000L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when - 100개 스레드에서 동시 입금
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    transactionFacade.deposit(DepositCommand.of(account.getId(), depositAmount));
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        // then - 모든 스레드 완료 및 최종 잔액 = 100 * 1000 = 100,000
        assertThat(completed).as("모든 스레드가 타임아웃 내에 완료되어야 함").isTrue();
        
        Account result = accountJpaRepository.findById(account.getId()).orElseThrow();
        assertThat(result.getBalance()).isEqualTo(threadCount * depositAmount);
    }

    @Test
    @DisplayName("동시성: A <-> B 왕복 이체 -> 데드락 없이 완료, 수수료만 차감")
    void concurrent_bidirectional_transfer_avoids_deadlock() throws Exception {
        // given - 두 계좌 각각 100,000원
        Account firstAccount = AccountFixtures.createAndSaveWithBalance(
                accountJpaRepository, "첫번째계좌", 100_000L
        );
        Account secondAccount = AccountFixtures.createAndSaveWithBalance(
                accountJpaRepository, "두번째계좌", 100_000L
        );

        int iterations = 50;
        long transferAmount = 500L;
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2 * iterations);

        // when - 첫번째 -> 두번째 50번, 두번째 -> 첫번째 50번 동시 실행
        for (int i = 0; i < iterations; i++) {
            // 첫번째 -> 두번째
            executorService.submit(() -> {
                try {
                    transactionFacade.transfer(TransferCommand.of(
                            firstAccount.getAccountNumber(),
                            secondAccount.getAccountNumber(),
                            transferAmount
                    ));
                } finally {
                    latch.countDown();
                }
            });

            // 두번째 -> 첫번째
            executorService.submit(() -> {
                try {
                    transactionFacade.transfer(TransferCommand.of(
                            secondAccount.getAccountNumber(),
                            firstAccount.getAccountNumber(),
                            transferAmount
                    ));
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(60, TimeUnit.SECONDS);
        executorService.shutdown();

        // then - 모든 스레드 완료, 데드락 없이 완료, 총 잔액 = 200,000 - (100건 * 수수료 5원)
        assertThat(completed).as("모든 스레드가 타임아웃 내에 완료되어야 함").isTrue();
        
        Account firstAccountResult = accountJpaRepository.findById(firstAccount.getId()).orElseThrow();
        Account secondAccountResult = accountJpaRepository.findById(secondAccount.getId()).orElseThrow();

        long totalBalance = firstAccountResult.getBalance() + secondAccountResult.getBalance();
        long expectedFee = 100 * 5; // 100건 * 1% of 500 = 5원
        assertThat(totalBalance).isEqualTo(200_000L - expectedFee);
    }
}
