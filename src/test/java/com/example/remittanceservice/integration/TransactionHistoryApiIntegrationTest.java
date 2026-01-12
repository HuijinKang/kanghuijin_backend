package com.example.remittanceservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.remittanceservice.TestcontainersConfiguration;
import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.fixture.AccountFixtures;
import com.example.remittanceservice.fixture.DatabaseFixtures;
import com.example.remittanceservice.infrastructure.account.AccountJpaRepository;
import com.example.remittanceservice.infrastructure.transactionpolicy.TransactionPolicyJpaRepository;
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
class TransactionHistoryApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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
    @DisplayName("입금 내역 조회: cursor pagination 동작")
    void deposit_history_cursorPaging() throws Exception {
        String accountNumber = AccountFixtures.generateAccountNumber();
        Account account = accountJpaRepository.save(AccountFixtures.createAccount(accountNumber, "테스트"));

        // 입금 3건 생성
        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", account.getId())
                        .header("X-Idempotency-Key", "dep-001")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"amount\":1000}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", account.getId())
                        .header("X-Idempotency-Key", "dep-002")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"amount\":2000}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", account.getId())
                        .header("X-Idempotency-Key", "dep-003")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"amount\":3000}"))
                .andExpect(status().isOk());

        // 첫 페이지 - 2개만 조회
        MvcResult page1 = mockMvc.perform(get("/api/v1/accounts/{accountId}/deposits?limit=2", account.getId()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode page1Body = objectMapper.readTree(page1.getResponse().getContentAsString());
        JsonNode data = page1Body.get("data");
        assertThat(data.get("items")).hasSize(2);
        assertThat(data.get("nextCursor")).isNotNull();

        // 다음 페이지 - 나머지 1개 조회
        String nextCursor = data.get("nextCursor").asText();
        mockMvc.perform(get("/api/v1/accounts/{accountId}/deposits?cursor={cursor}&limit=2", account.getId(), nextCursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(1));
    }
}
