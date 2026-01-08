package com.example.remittanceservice.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.remittanceservice.application.command.CreateAccountCommand;
import com.example.remittanceservice.application.port.out.AccountRepository;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import com.example.remittanceservice.domain.account.Account;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
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
        when(accountRepository.findByAccountNumber("123456789012"))
                .thenReturn(Optional.of(Account.create("123456789012", "홍길동")));

        assertThatThrownBy(() -> accountService.create(CreateAccountCommand.of("123456789012", "홍길동")))
                .isInstanceOf(CoreException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_ACCOUNT);
    }

    @Test
    @DisplayName("계좌 생성: 저장 시 UNIQUE 충돌이면 DUPLICATE_ACCOUNT")
    void create_uniqueViolation_throws() {
        when(accountRepository.findByAccountNumber("123456789012"))
                .thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> accountService.create(CreateAccountCommand.of("123456789012", "홍길동")))
                .isInstanceOf(CoreException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_ACCOUNT);
    }

    @Test
    @DisplayName("계좌 삭제(해지): 없는 계좌면 NOT_FOUND")
    void delete_notFound_throws() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.delete(999L))
                .isInstanceOf(CoreException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("계좌 삭제(해지): 이미 CLOSED면 멱등하게 성공(추가 저장 없이 종료)")
    void delete_alreadyClosed_isIdempotent() {
        Account closed = Account.create("123456789012", "홍길동");
        closed.close();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(closed));

        accountService.delete(1L);

        verify(accountRepository).findById(1L);
    }
}
