package com.example.remittanceservice.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.remittanceservice.TestcontainersConfiguration;
import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.domain.policy.PolicyConfig;
import com.example.remittanceservice.domain.policy.PolicyType;
import com.example.remittanceservice.infrastructure.account.AccountJpaRepository;
import com.example.remittanceservice.infrastructure.policy.PolicyConfigJpaRepository;
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
@SuppressWarnings("null")
class TransactionApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountJpaRepository accountJpaRepository;

    @Autowired
    TransactionJpaRepository transactionJpaRepository;

    @Autowired
    PolicyConfigJpaRepository policyConfigJpaRepository;

    @BeforeEach
    void setUp() {
        transactionJpaRepository.deleteAll();
        accountJpaRepository.deleteAll();

        PolicyConfig policy = policyConfigJpaRepository.findByPolicyType(PolicyType.DEFAULT)
                .orElseGet(() -> policyConfigJpaRepository.save(PolicyConfig.of(PolicyType.DEFAULT, 1_000_000L, 3_000_000L, 100)));

        policy.update(1_000_000L, 3_000_000L, 100);
        policyConfigJpaRepository.save(policy);
    }

    @Test
    @DisplayName("입금/출금/이체 통합: 잔액 반영 + 수수료(1%) 적용")
    void depositWithdrawTransfer_flow() throws Exception {
        String fromNo = String.valueOf(Math.abs(System.nanoTime())).substring(0, 12);
        String toNo = String.valueOf(Math.abs(System.nanoTime() + 1)).substring(0, 12);

        Account from = accountJpaRepository.save(Account.create(fromNo, "보내는사람"));
        Account to = accountJpaRepository.save(Account.create(toNo, "받는사람"));

        // deposit 2,000 to from
        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", from.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                { "amount": 2000 }
                                """))
                .andExpect(status().isNoContent());

        // withdraw 500
        mockMvc.perform(post("/api/v1/accounts/{accountId}/withdrawals", from.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                { "amount": 500 }
                                """))
                .andExpect(status().isNoContent());

        // transfer 1,000 (fee 1% => 10)
        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {
                                  "fromAccountNumber": "%s",
                                  "toAccountNumber": "%s",
                                  "amount": 1000
                                }
                                """.formatted(fromNo, toNo)))
                .andExpect(status().isNoContent());

        Account fromAfter = accountJpaRepository.findById(from.getId()).orElseThrow();
        Account toAfter = accountJpaRepository.findById(to.getId()).orElseThrow();

        // 2000 - 500 - (1000 + 10) = 490
        assertThat(fromAfter.getBalance()).isEqualTo(490L);
        assertThat(toAfter.getBalance()).isEqualTo(1000L);
    }
}
