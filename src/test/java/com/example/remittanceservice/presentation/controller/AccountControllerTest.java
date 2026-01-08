package com.example.remittanceservice.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.remittanceservice.application.command.CreateAccountCommand;
import com.example.remittanceservice.application.dto.AccountResult;
import com.example.remittanceservice.application.facade.AccountFacade;
import com.example.remittanceservice.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AccountFacade accountFacade;

    @Test
    @DisplayName("계좌 생성 API: 성공 시 201 + 응답 바디")
    void createAccount_success_201() throws Exception {
        when(accountFacade.createAccount(any(CreateAccountCommand.class)))
                .thenReturn(AccountResult.of(1L, "123456789012", "홍길동"));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {
                                  "accountNumber": "123456789012",
                                  "ownerName": "홍길동"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("123456789012"))
                .andExpect(jsonPath("$.ownerName").value("홍길동"));
    }

    @Test
    @DisplayName("계좌 생성 API: 계좌번호 형식 오류면 400")
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
    @DisplayName("계좌 삭제(해지) API: 성공 시 204")
    void deleteAccount_success_204() throws Exception {
        mockMvc.perform(delete("/api/v1/accounts/1"))
                .andExpect(status().isNoContent());

        verify(accountFacade).deleteAccount(1L);
    }
}
