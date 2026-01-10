package com.example.remittanceservice.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.remittanceservice.application.command.CreateAccountCommand;
import com.example.remittanceservice.domain.account.AccountRepository;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    AccountRepository accountRepository;

    @InjectMocks
    AccountService accountService;

    @Test
    @DisplayName("계좌 생성: 이미 존재하는 계좌번호면 DUPLICATE_ACCOUNT")
    void create_duplicateAccountNumber_throws() {
        when(accountRepository.existsByAccountNumber("123456789012"))
                .thenReturn(true);

        assertThatThrownBy(() -> accountService.create(
                CreateAccountCommand.of(
                        "123456789012",
                        "홍길동"
                )
        ))
                .isInstanceOf(CoreException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_ACCOUNT);
    }

    @Test
    @DisplayName("계좌 삭제: 없는 계좌면 NOT_FOUND")
    void delete_notFound_throws() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.delete(999L))
                .isInstanceOf(CoreException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
