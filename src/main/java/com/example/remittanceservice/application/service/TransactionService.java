package com.example.remittanceservice.application.service;

import com.example.remittanceservice.application.command.DepositCommand;
import com.example.remittanceservice.domain.account.AccountRepository;
import com.example.remittanceservice.domain.transaction.TransactionRepository;
import com.example.remittanceservice.application.command.TransferCommand;
import com.example.remittanceservice.application.command.WithdrawCommand;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.domain.transaction.Transaction;
import com.example.remittanceservice.domain.transaction.TransactionStatus;
import com.example.remittanceservice.domain.transaction.TransactionType;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void deposit(DepositCommand command) {
        Account account = accountRepository.findByIdForUpdate(command.accountId())
                .orElseThrow(() -> new CoreException(ErrorCode.NOT_FOUND));

        account.deposit(command.amount());
        accountRepository.save(account);

        transactionRepository.save(Transaction.deposit(account, command.amount()));
    }

    @Transactional
    public void withdraw(WithdrawCommand command, long withdrawDailyLimit) {
        Account account = accountRepository.findByIdForUpdate(command.accountId())
                .orElseThrow(() -> new CoreException(ErrorCode.NOT_FOUND));

        ZonedDateTime start = todayStartUtc();
        ZonedDateTime end = start.plusDays(1);
        long todayTotal = transactionRepository.sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
                account.getId(),
                TransactionType.WITHDRAW,
                TransactionStatus.SUCCESS,
                start,
                end
        );
        if (todayTotal + command.amount() > withdrawDailyLimit) {
            throw new CoreException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }

        account.withdraw(command.amount());
        accountRepository.save(account);

        transactionRepository.save(Transaction.withdraw(account, command.amount()));
    }

    @Transactional
    public void transfer(TransferCommand command, long transferDailyLimit, int transferFeeBps) {
        // 1) 입력 검증
        if (command.fromAccountNumber().equals(command.toAccountNumber())) {
            throw new CoreException(ErrorCode.VALIDATION_ERROR, "from/to 계좌번호는 서로 달라야 합니다.");
        }

        // 2) 락 순서 결정(데드락 방지)
        String firstKey = command.fromAccountNumber().compareTo(command.toAccountNumber()) <= 0
                ? command.fromAccountNumber()
                : command.toAccountNumber();
        String secondKey = firstKey.equals(command.fromAccountNumber())
                ? command.toAccountNumber()
                : command.fromAccountNumber();

        // 3) 계좌 락 획득 (PESSIMISTIC_WRITE)
        Account first = accountRepository.findByAccountNumberForUpdate(firstKey)
                .orElseThrow(() -> new CoreException(ErrorCode.NOT_FOUND));
        Account second = accountRepository.findByAccountNumberForUpdate(secondKey)
                .orElseThrow(() -> new CoreException(ErrorCode.NOT_FOUND));

        // 4) 송금/수취 계좌 매핑
        Account from = command.fromAccountNumber().equals(first.getAccountNumber()) ? first : second;
        Account to = command.toAccountNumber().equals(first.getAccountNumber()) ? first : second;

        // 5) 계좌 상태 체크
        if (from.isClosed() || to.isClosed()) {
            throw new CoreException(ErrorCode.ACCOUNT_CLOSED);
        }

        // 6) 일 한도 체크 (UTC 날짜 기준)
        ZonedDateTime start = todayStartUtc();
        ZonedDateTime end = start.plusDays(1);
        long todayTransferTotal = transactionRepository.sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
                from.getId(),
                TransactionType.TRANSFER_OUT,
                TransactionStatus.SUCCESS,
                start,
                end
        );
        if (todayTransferTotal + command.amount() > transferDailyLimit) {
            throw new CoreException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }

        // 7) 수수료 계산 및 잔액 반영
        long fee = calculateFee(command.amount(), transferFeeBps);
        from.withdraw(command.amount() + fee);
        to.deposit(command.amount());

        // 8) 저장 및 거래내역 기록
        accountRepository.save(from);
        accountRepository.save(to);

        transactionRepository.save(
                Transaction.transferOut(from, to.getAccountNumber(), command.amount(), fee)
        );
        transactionRepository.save(
                Transaction.transferIn(to, from.getAccountNumber(), command.amount())
        );
    }

    private static ZonedDateTime todayStartUtc() {
        return LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC);
    }

    private static long calculateFee(long amount, int feeBps) {
        // feeBps: 100 bps = 1.00%
        return (amount * feeBps) / 10_000L;
    }
}
