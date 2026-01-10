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
    @DisplayName("계좌 생성/삭제 통합 테스트: 생성 201, 중복 409, 삭제 204")
    void accountCreateDeleteFlow() throws Exception {
        String accountNumber = String.valueOf(System.nanoTime()).substring(0, 12);

        // create (201)
        MvcResult created = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {
                                  "accountNumber": "%s",
                                  "ownerName": "홍길동"
                                }
                                """.formatted(accountNumber)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(created.getResponse().getContentAsString());
        long accountId = createdJson.get("accountId").asLong();

        // duplicate (409)
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {
                                  "accountNumber": "%s",
                                  "ownerName": "홍길동"
                                }
                                """.formatted(accountNumber)))
                .andExpect(status().isConflict());

        // delete (204)
        mockMvc.perform(delete("/api/v1/accounts/{accountId}", accountId))
                .andExpect(status().isNoContent());

        // delete 멱등성 체크 (204)
        mockMvc.perform(delete("/api/v1/accounts/{accountId}", accountId))
                .andExpect(status().isNoContent());

        // DB state check
        Account account = accountJpaRepository.findById(accountId).orElseThrow();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
        assertThat(account.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("계좌 생성 통합: 계좌번호 형식 오류면 400")
    void createAccount_invalidAccountNumber_400() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {
                                  "accountNumber": "123-456",
                                  "ownerName": "홍길동"
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
