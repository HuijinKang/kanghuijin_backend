package com.example.remittanceservice.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.remittanceservice.application.command.CreateAccountCommand;
import com.example.remittanceservice.domain.account.AccountRepository;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import com.example.remittanceservice.domain.account.Account;
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
    @DisplayName("계좌 생성: 서버가 12자리 계좌번호를 생성한다")
    void create_generates12DigitAccountNumber() {
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account created = accountService.create(CreateAccountCommand.of("홍길동", "01012345678"));

        assertThat(created.getAccountNumber()).hasSize(12);
        assertThat(created.getOwnerName()).isEqualTo("홍길동");
        assertThat(created.getPhoneNumber()).isEqualTo("01012345678");
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
