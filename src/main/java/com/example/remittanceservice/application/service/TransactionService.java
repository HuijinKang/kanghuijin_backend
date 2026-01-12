package com.example.remittanceservice.application.service;

import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.domain.transaction.TransactionRepository;
import com.example.remittanceservice.domain.transaction.Transaction;
import com.example.remittanceservice.domain.transaction.TransactionStatus;
import com.example.remittanceservice.domain.transaction.TransactionType;
import com.example.remittanceservice.domain.transaction.TransferRoute;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public Optional<Transaction> findByTransferRouteAndIdempotencyKey(
            TransferRoute transferRoute,
            String idempotencyKey
    ) {
        return transactionRepository.findByTransferRouteAndIdempotencyKey(transferRoute, idempotencyKey);
    }

    @Transactional
    public void recordDeposit(Account account, long amount, TransferRoute transferRoute, String idempotencyKey) {
        transactionRepository.save(Transaction.deposit(account, amount, transferRoute, idempotencyKey));
    }

    @Transactional
    public void recordWithdraw(Account account, long amount, TransferRoute transferRoute, String idempotencyKey) {
        transactionRepository.save(Transaction.withdraw(account, amount, transferRoute, idempotencyKey));
    }

    @Transactional
    public void recordTransfer(
            Account senderAccount,
            Account receiverAccount,
            long amount,
            long fee,
            TransferRoute transferRoute,
            String idempotencyKey
    ) {
        transactionRepository.save(
                Transaction.transferOut(
                        senderAccount,
                        receiverAccount.getAccountNumber(),
                        amount,
                        fee,
                        transferRoute,
                        idempotencyKey
                )
        );
        transactionRepository.save(
                Transaction.transferIn(
                        receiverAccount,
                        senderAccount.getAccountNumber(),
                        amount,
                        transferRoute
                )
        );
    }

    @Transactional(readOnly = true)
    public long getTodayWithdrawTotal(long accountId) {
        ZonedDateTime start = todayStartUtc();
        ZonedDateTime end = start.plusDays(1);
        return transactionRepository.sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
                accountId,
                TransactionType.WITHDRAW,
                TransactionStatus.SUCCESS,
                start,
                end
        );
    }

    @Transactional(readOnly = true)
    public long getTodayTransferTotal(long accountId) {
        ZonedDateTime start = todayStartUtc();
        ZonedDateTime end = start.plusDays(1);
        return transactionRepository.sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
                accountId,
                TransactionType.TRANSFER_OUT,
                TransactionStatus.SUCCESS,
                start,
                end
        );
    }

    private static ZonedDateTime todayStartUtc() {
        return LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC);
    }
}
