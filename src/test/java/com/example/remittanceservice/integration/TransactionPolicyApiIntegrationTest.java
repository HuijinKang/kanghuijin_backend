package com.example.remittanceservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.remittanceservice.TestcontainersConfiguration;
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
class TransactionPolicyApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("정책 Upsert API: PUT 후 GET(/policies/{policyType})으로 조회 가능")
    void upsertPolicy_thenGet() throws Exception {
        mockMvc.perform(put("/api/v1/policies/DEFAULT")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {
                                  "withdrawDailyLimit": 1000000,
                                  "transferDailyLimit": 3000000,
                                  "transferFeeBps": 100
                                }
                                """))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/v1/policies/DEFAULT"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode data = body.get("data");
        assertThat(data.get("policyType").asText()).isEqualTo("DEFAULT");
        assertThat(data.get("withdrawDailyLimit").asLong()).isEqualTo(1_000_000L);
        assertThat(data.get("transferDailyLimit").asLong()).isEqualTo(3_000_000L);
        assertThat(data.get("transferFeeBps").asInt()).isEqualTo(100);
    }
}
