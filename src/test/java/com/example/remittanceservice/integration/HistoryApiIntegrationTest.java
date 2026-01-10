package com.example.remittanceservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.remittanceservice.TestcontainersConfiguration;
import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.domain.policy.PolicyConfig;
import com.example.remittanceservice.domain.policy.PolicyType;
import com.example.remittanceservice.infrastructure.account.AccountJpaRepository;
import com.example.remittanceservice.infrastructure.policy.PolicyConfigJpaRepository;
import com.example.remittanceservice.infrastructure.transaction.TransactionJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class HistoryApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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
    @DisplayName("입금 내역 조회: cursor pagination 동작")
    void deposit_history_cursorPaging() throws Exception {
        String accountNumber = String.valueOf(Math.abs(System.nanoTime())).substring(0, 12);
        Account account = accountJpaRepository.save(Account.create(accountNumber, "테스트"));

        // 입금 3건 생성
        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", account.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"amount\":1000}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", account.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"amount\":2000}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", account.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"amount\":3000}"))
                .andExpect(status().isNoContent());

        // 첫 페이지 - 2개만 조회
        MvcResult page1 = mockMvc.perform(get("/api/v1/accounts/{accountId}/deposits?limit=2", account.getId()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode page1Body = objectMapper.readTree(page1.getResponse().getContentAsString());
        assertThat(page1Body.get("items")).hasSize(2);
        assertThat(page1Body.get("nextCursor")).isNotNull();

        // 다음 페이지 - 나머지 1개 조회
        long nextCursor = page1Body.get("nextCursor").asLong();
        mockMvc.perform(get("/api/v1/accounts/{accountId}/deposits?cursor={cursor}&limit=2", account.getId(), nextCursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1));
    }
}

