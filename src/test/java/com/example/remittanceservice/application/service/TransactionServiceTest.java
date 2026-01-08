package com.example.remittanceservice.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.remittanceservice.application.command.DepositCommand;
import com.example.remittanceservice.application.command.TransferCommand;
import com.example.remittanceservice.application.command.WithdrawCommand;
import com.example.remittanceservice.domain.account.AccountRepository;
import com.example.remittanceservice.domain.transaction.TransactionRepository;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.domain.transaction.Transaction;
import com.example.remittanceservice.domain.transaction.TransactionStatus;
import com.example.remittanceservice.domain.transaction.TransactionType;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    TransactionRepository transactionRepository;

    @InjectMocks
    TransactionService transactionService;

    @Test
    @DisplayName("입금: 성공 시 저장이 호출된다")
    void deposit_success_savesTransaction() {
        Account account = mock(Account.class);
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));

        transactionService.deposit(new DepositCommand(1L, 500L));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("출금: 일 한도를 초과하면 DAILY_LIMIT_EXCEEDED")
    void withdraw_dailyLimitExceeded_throws() {
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(1L);
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));

        when(transactionRepository.sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
                eq(1L),
                eq(TransactionType.WITHDRAW),
                eq(TransactionStatus.SUCCESS),
                any(ZonedDateTime.class),
                any(ZonedDateTime.class)
        )).thenReturn(99L);

        assertThatThrownBy(() -> transactionService.withdraw(new WithdrawCommand(1L, 2L), 100L))
                .isInstanceOf(CoreException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DAILY_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("이체: 수수료 포함 잔액이 부족하면 INSUFFICIENT_BALANCE")
    void transfer_insufficientBalance_withFee_throws() {
        Account fromAccount = mock(Account.class);
        when(fromAccount.getId()).thenReturn(1L);
        when(fromAccount.getAccountNumber()).thenReturn("111111111111");
        when(fromAccount.isClosed()).thenReturn(false);
        doThrow(new CoreException(ErrorCode.INSUFFICIENT_BALANCE)).when(fromAccount).withdraw(101L);

        Account toAccount = mock(Account.class);
        when(toAccount.isClosed()).thenReturn(false);

        when(accountRepository.findByAccountNumberForUpdate("111111111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumberForUpdate("222222222222")).thenReturn(Optional.of(toAccount));

        when(transactionRepository.sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
                eq(1L),
                eq(TransactionType.TRANSFER_OUT),
                eq(TransactionStatus.SUCCESS),
                any(ZonedDateTime.class),
                any(ZonedDateTime.class)
        )).thenReturn(0L);

        assertThatThrownBy(() -> transactionService.transfer(new TransferCommand("111111111111", "222222222222", 100L), 3_000_000L, 100))
                .isInstanceOf(CoreException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_BALANCE);
    }
}
