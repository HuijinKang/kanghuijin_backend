package com.example.remittanceservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.remittanceservice.TestcontainersConfiguration;
import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.fixture.AccountFixtures;
import com.example.remittanceservice.fixture.DatabaseFixtures;
import com.example.remittanceservice.infrastructure.account.AccountJpaRepository;
import com.example.remittanceservice.infrastructure.transactionpolicy.TransactionPolicyJpaRepository;
import com.example.remittanceservice.infrastructure.transaction.TransactionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class TransactionApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountJpaRepository accountJpaRepository;

    @Autowired
    TransactionJpaRepository transactionJpaRepository;

    @Autowired
    TransactionPolicyJpaRepository transactionPolicyJpaRepository;

    @BeforeEach
    void setUp() {
        DatabaseFixtures.setupTestEnvironment(
                accountJpaRepository,
                transactionJpaRepository,
                transactionPolicyJpaRepository
        );
    }

    @Test
    @DisplayName("입금/출금/이체 통합: 잔액 반영 + 수수료(1%) 적용")
    void depositWithdrawTransfer_flow() throws Exception {
        String senderAccountNumber = AccountFixtures.generateAccountNumber();
        String receiverAccountNumber = AccountFixtures.generateAccountNumber();

        Account senderAccount = accountJpaRepository.save(AccountFixtures.createAccount(senderAccountNumber, "보내는사람"));
        Account receiverAccount = accountJpaRepository.save(AccountFixtures.createAccount(receiverAccountNumber, "받는사람"));

        // deposit 2,000 to sender
        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", senderAccount.getId())
                        .header("X-Idempotency-Key", "dep-001")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                { "amount": 2000 }
                                """))
                .andExpect(status().isOk());

        // withdraw 500
        mockMvc.perform(post("/api/v1/accounts/{accountId}/withdrawals", senderAccount.getId())
                        .header("X-Idempotency-Key", "wdr-001")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                { "amount": 500 }
                                """))
                .andExpect(status().isOk());

        // transfer 1,000 (fee 1% => 10)
        mockMvc.perform(post("/api/v1/transfers")
                        .header("X-Idempotency-Key", "trx-001")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {
                                  "fromAccountNumber": "%s",
                                  "toAccountNumber": "%s",
                                  "amount": 1000
                                }
                                """.formatted(senderAccountNumber, receiverAccountNumber)))
                .andExpect(status().isOk());

        Account senderAccountAfter = accountJpaRepository.findById(senderAccount.getId()).orElseThrow();
        Account receiverAccountAfter = accountJpaRepository.findById(receiverAccount.getId()).orElseThrow();

        // 2000 - 500 - (1000 + 10) = 490
        assertThat(senderAccountAfter.getBalance()).isEqualTo(490L);
        assertThat(receiverAccountAfter.getBalance()).isEqualTo(1000L);
    }
}
