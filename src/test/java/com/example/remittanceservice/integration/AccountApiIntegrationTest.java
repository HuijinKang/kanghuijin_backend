package com.example.remittanceservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.remittanceservice.TestcontainersConfiguration;
import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.domain.account.AccountStatus;
import com.example.remittanceservice.infrastructure.account.AccountJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class AccountApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountJpaRepository accountJpaRepository;

    @Test
    @DisplayName("계좌 생성/삭제 통합 테스트: 생성 201, 동일 전화번호도 추가 생성 가능 201, 삭제 200")
    void accountCreateDeleteFlow() throws Exception {
        // create (201)
        MvcResult created = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {
                                  "ownerName": "홍길동",
                                  "phoneNumber": "01012345678"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(created.getResponse().getContentAsString());
        JsonNode data = createdJson.get("data");
        long accountId = data.get("accountId").asLong();
        String createdAccountNumber = data.get("accountNumber").asText();
        assertThat(createdAccountNumber).hasSize(12);

        // same phoneNumber (201) - 한 명이 여러 계좌를 가질 수 있음
        MvcResult createdSecond = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {
                                  "ownerName": "홍길동",
                                  "phoneNumber": "01012345678"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdSecondJson = objectMapper.readTree(createdSecond.getResponse().getContentAsString());
        String secondAccountNumber = createdSecondJson.get("data").get("accountNumber").asText();
        assertThat(secondAccountNumber).hasSize(12);
        assertThat(secondAccountNumber).isNotEqualTo(createdAccountNumber);

        // delete (200)
        mockMvc.perform(delete("/api/v1/accounts/{accountId}", accountId))
                .andExpect(status().isOk());

        // delete 멱등성 체크 (200)
        mockMvc.perform(delete("/api/v1/accounts/{accountId}", accountId))
                .andExpect(status().isOk());

        // DB state check
        Account account = accountJpaRepository.findById(accountId).orElseThrow();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
        assertThat(account.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("계좌 생성 통합: 전화번호 형식 오류면 400")
    void createAccount_invalidPhoneNumber_400() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {
                                  "ownerName": "홍길동",
                                  "phoneNumber": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("계좌 삭제 통합: 없는 계좌면 404")
    void deleteAccount_notFound_404() throws Exception {
        mockMvc.perform(delete("/api/v1/accounts/99999999"))
                .andExpect(status().isNotFound());
    }
}
