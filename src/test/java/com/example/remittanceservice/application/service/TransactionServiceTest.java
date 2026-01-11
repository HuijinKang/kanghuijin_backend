package com.example.remittanceservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.domain.transaction.Transaction;
import com.example.remittanceservice.domain.transaction.TransactionRepository;
import com.example.remittanceservice.domain.transaction.TransactionStatus;
import com.example.remittanceservice.domain.transaction.TransactionType;
import com.example.remittanceservice.fixture.AccountFixtures;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    TransactionRepository transactionRepository;

    @InjectMocks
    TransactionService transactionService;

    @Test
    @DisplayName("입금 거래 기록: 저장이 호출된다")
    void recordDeposit_savesTransaction() {
        Account account = AccountFixtures.createAccount("111111111111", "테스트");

        transactionService.recordDeposit(account, 500L);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("출금 거래 기록: 저장이 호출된다")
    void recordWithdraw_savesTransaction() {
        Account account = AccountFixtures.createAccount("111111111111", "테스트");

        transactionService.recordWithdraw(account, 500L);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("이체 거래 기록: 송금/수취 2건이 저장된다")
    void recordTransfer_savesTwoTransactions() {
        Account senderAccount = AccountFixtures.createAccount("111111111111", "송금인");
        Account receiverAccount = AccountFixtures.createAccount("222222222222", "수취인");

        transactionService.recordTransfer(senderAccount, receiverAccount, 1000L, 10L);

        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("오늘 출금 총액 조회: repository 결과를 반환한다")
    void getTodayWithdrawTotal_returnsSum() {
        when(transactionRepository.sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
                eq(1L),
                eq(TransactionType.WITHDRAW),
                eq(TransactionStatus.SUCCESS),
                any(ZonedDateTime.class),
                any(ZonedDateTime.class)
        )).thenReturn(500L);

        long result = transactionService.getTodayWithdrawTotal(1L);

        assertThat(result).isEqualTo(500L);
    }

    @Test
    @DisplayName("오늘 이체 총액 조회: repository 결과를 반환한다")
    void getTodayTransferTotal_returnsSum() {
        when(transactionRepository.sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
                eq(1L),
                eq(TransactionType.TRANSFER_OUT),
                eq(TransactionStatus.SUCCESS),
                any(ZonedDateTime.class),
                any(ZonedDateTime.class)
        )).thenReturn(1000L);

        long result = transactionService.getTodayTransferTotal(1L);

        assertThat(result).isEqualTo(1000L);
    }
}
