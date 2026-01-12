package com.example.remittanceservice.application.facade;

import com.example.remittanceservice.application.command.DepositCommand;
import com.example.remittanceservice.application.command.TransferCommand;
import com.example.remittanceservice.application.command.WithdrawCommand;
import com.example.remittanceservice.application.service.AccountService;
import com.example.remittanceservice.application.service.InternalTransferHandler;
import com.example.remittanceservice.application.service.TransactionPolicyService;
import com.example.remittanceservice.application.service.TransactionService;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.domain.transaction.Transaction;
import com.example.remittanceservice.domain.transaction.TransactionType;
import com.example.remittanceservice.domain.transaction.TransferRoute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionFacade {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final TransactionPolicyService transactionPolicyService;
    private final InternalTransferHandler internalTransferHandler;

    @Transactional(isolation = Isolation.SERIALIZABLE, timeout = 10)
    public void deposit(DepositCommand command, String idempotencyKey) {
        log.info("[DEPOSIT_START] accountId={}, amount={}", command.accountId(), command.amount());
        TransferRoute route = TransferRoute.INTERNAL_CORE;
        
        try {
            // 1. 계좌 조회 및 락 획득
            Account account = accountService.findByIdForUpdate(command.accountId());

            // 2. 멱등성 체크
            Transaction existing = transactionService.findByTransferRouteAndIdempotencyKey(route, idempotencyKey)
                    .orElse(null);
            if (existing != null) {
                validateIdempotencyMatch(existing, TransactionType.DEPOSIT, account, command.amount(), null);
                log.info("[DEPOSIT_IDEMPOTENT_HIT] accountId={}, idempotencyKey={}", command.accountId(), idempotencyKey);
                return;
            }
            
            // 3. 입금 실행
            accountService.deposit(account, command.amount());
            
            // 4. 거래 기록
            transactionService.recordDeposit(account, command.amount(), route, idempotencyKey);
            
            log.info("[DEPOSIT_SUCCESS] accountId={}, amount={}, newBalance={}", 
                    command.accountId(), command.amount(), account.getBalance());
        } catch (DataIntegrityViolationException e) {
            log.warn("[DEPOSIT_IDEMPOTENCY_CONFLICT] accountId={}, idempotencyKey={}, error={}",
                    command.accountId(), idempotencyKey, e.getMessage());
            throw new CoreException(ErrorCode.IDEMPOTENCY_CONFLICT, "duplicate idempotency key");
        } catch (Exception e) {
            log.error("[DEPOSIT_FAILED] accountId={}, amount={}, error={}", 
                    command.accountId(), command.amount(), e.getMessage());
            throw e;
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, timeout = 10)
    public void withdraw(WithdrawCommand command, String idempotencyKey) {
        log.info("[WITHDRAW_START] accountId={}, amount={}", command.accountId(), command.amount());
        TransferRoute route = TransferRoute.INTERNAL_CORE;
        
        try {
            // 1. 정책 조회
            long withdrawDailyLimit = transactionPolicyService.getWithdrawDailyLimit();
            
            // 2. 일 한도 체크
            long todayTotal = transactionService.getTodayWithdrawTotal(command.accountId());
            if (todayTotal + command.amount() > withdrawDailyLimit) {
                log.warn("[WITHDRAW_LIMIT_EXCEEDED] accountId={}, todayTotal={}, requestAmount={}, limit={}", 
                        command.accountId(), todayTotal, command.amount(), withdrawDailyLimit);
                throw new CoreException(ErrorCode.DAILY_LIMIT_EXCEEDED);
            }
            
            // 3. 계좌 조회 및 락 획득
            Account account = accountService.findByIdForUpdate(command.accountId());

            // 4. 멱등성 체크
            Transaction existing = transactionService.findByTransferRouteAndIdempotencyKey(route, idempotencyKey)
                    .orElse(null);
            if (existing != null) {
                validateIdempotencyMatch(existing, TransactionType.WITHDRAW, account, command.amount(), null);
                log.info("[WITHDRAW_IDEMPOTENT_HIT] accountId={}, idempotencyKey={}", command.accountId(), idempotencyKey);
                return;
            }
            
            // 5. 출금 실행
            accountService.withdraw(account, command.amount());
            
            // 6. 거래 기록
            transactionService.recordWithdraw(account, command.amount(), route, idempotencyKey);
            
            log.info("[WITHDRAW_SUCCESS] accountId={}, amount={}, newBalance={}, todayTotal={}", 
                    command.accountId(), command.amount(), account.getBalance(), todayTotal + command.amount());
        } catch (DataIntegrityViolationException e) {
            log.warn("[WITHDRAW_IDEMPOTENCY_CONFLICT] accountId={}, idempotencyKey={}, error={}",
                    command.accountId(), idempotencyKey, e.getMessage());
            throw new CoreException(ErrorCode.IDEMPOTENCY_CONFLICT, "duplicate idempotency key");
        } catch (Exception e) {
            log.error("[WITHDRAW_FAILED] accountId={}, amount={}, error={}", 
                    command.accountId(), command.amount(), e.getMessage());
            throw e;
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, timeout = 10)
    public void transfer(TransferCommand transferCommand, String idempotencyKey) {
        log.info("[TRANSFER_START] from={}, to={}, amount={}", 
                transferCommand.fromAccountNumber(), transferCommand.toAccountNumber(), transferCommand.amount());
        TransferRoute route = TransferRoute.INTERNAL_CORE;
        
        try {
            // 1. 입력 검증
            if (transferCommand.fromAccountNumber().equals(transferCommand.toAccountNumber())) {
                throw new CoreException(ErrorCode.VALIDATION_ERROR, "from/to 계좌번호는 서로 달라야 합니다.");
            }

            // 2. 락 순서 결정 (데드락 방지): 계좌번호 오름차순으로 락 획득
            String smallerAccountNumber = transferCommand.fromAccountNumber().compareTo(transferCommand.toAccountNumber()) <= 0
                    ? transferCommand.fromAccountNumber()
                    : transferCommand.toAccountNumber();
            String largerAccountNumber = smallerAccountNumber.equals(transferCommand.fromAccountNumber())
                    ? transferCommand.toAccountNumber()
                    : transferCommand.fromAccountNumber();

            // 3. 계좌 락 획득 (PESSIMISTIC_WRITE): 작은 번호 -> 큰 번호 순서로 락
            Account accountWithSmallerNumber = accountService.findByAccountNumberForUpdate(smallerAccountNumber);
            Account accountWithLargerNumber = accountService.findByAccountNumberForUpdate(largerAccountNumber);

            // 4. 송금/수취 계좌 매핑
            Account senderAccount = transferCommand.fromAccountNumber().equals(accountWithSmallerNumber.getAccountNumber()) 
                    ? accountWithSmallerNumber 
                    : accountWithLargerNumber;
            Account receiverAccount = transferCommand.toAccountNumber().equals(accountWithSmallerNumber.getAccountNumber()) 
                    ? accountWithSmallerNumber 
                    : accountWithLargerNumber;

            // 5. 멱등성 체크
            Transaction existing = transactionService.findByTransferRouteAndIdempotencyKey(route, idempotencyKey)
                    .orElse(null);
            if (existing != null) {
                validateIdempotencyMatch(
                        existing,
                        TransactionType.TRANSFER_OUT,
                        senderAccount,
                        transferCommand.amount(),
                        transferCommand.toAccountNumber()
                );
                log.info("[TRANSFER_IDEMPOTENT_HIT] from={}, idempotencyKey={}",
                        transferCommand.fromAccountNumber(), idempotencyKey);
                return;
            }

            // 6. 실행 경로 추후 타행/해외 확장
            internalTransferHandler.handle(transferCommand);

            // 6. 계좌 상태 체크
            accountService.validateAccountActive(senderAccount);
            accountService.validateAccountActive(receiverAccount);

            // 7. 정책 조회 (한도/수수료)
            long transferDailyLimit = transactionPolicyService.getTransferDailyLimit();
            long fee = transactionPolicyService.calculateTransferFee(transferCommand.amount());

            // 8. 일 한도 체크
            long todayTransferTotal = transactionService.getTodayTransferTotal(senderAccount.getId());
            if (todayTransferTotal + transferCommand.amount() > transferDailyLimit) {
                log.warn("[TRANSFER_LIMIT_EXCEEDED] fromAccountId={}, todayTotal={}, requestAmount={}, limit={}", 
                        senderAccount.getId(), todayTransferTotal, transferCommand.amount(), transferDailyLimit);
                throw new CoreException(ErrorCode.DAILY_LIMIT_EXCEEDED);
            }

            // 9. 잔액 반영
            accountService.transferMoney(senderAccount, receiverAccount, transferCommand.amount(), fee);

            // 10. 거래 기록
            transactionService.recordTransfer(senderAccount, receiverAccount, transferCommand.amount(), fee, route, idempotencyKey);
            
            log.info("[TRANSFER_SUCCESS] from={}, to={}, amount={}, fee={}, fromBalance={}, toBalance={}", 
                    transferCommand.fromAccountNumber(), transferCommand.toAccountNumber(),
                    transferCommand.amount(), fee, senderAccount.getBalance(), receiverAccount.getBalance());
        } catch (DataIntegrityViolationException e) {
            log.warn("[TRANSFER_IDEMPOTENCY_CONFLICT] from={}, idempotencyKey={}, error={}",
                    transferCommand.fromAccountNumber(), idempotencyKey, e.getMessage());
            throw new CoreException(ErrorCode.IDEMPOTENCY_CONFLICT, "duplicate idempotency key");
        } catch (Exception e) {
            log.error("[TRANSFER_FAILED] from={}, to={}, amount={}, error={}", 
                    transferCommand.fromAccountNumber(), transferCommand.toAccountNumber(),
                    transferCommand.amount(), e.getMessage());
            throw e;
        }
    }

    private static void validateIdempotencyMatch(
            Transaction existing,
            TransactionType expectedType,
            Account expectedAccount,
            long expectedAmount,
            String expectedCounterpartyAccountNumber
    ) {
        if (existing.getType() != expectedType) {
            throw new CoreException(ErrorCode.IDEMPOTENCY_CONFLICT, "idempotency key request mismatch(type)");
        }
        if (existing.getAccount() == null || existing.getAccount().getId() == null || !existing.getAccount().getId().equals(expectedAccount.getId())) {
            throw new CoreException(ErrorCode.IDEMPOTENCY_CONFLICT, "idempotency key request mismatch(account)");
        }
        if (existing.getAmount() != expectedAmount) {
            throw new CoreException(ErrorCode.IDEMPOTENCY_CONFLICT, "idempotency key request mismatch(amount)");
        }
        if (expectedCounterpartyAccountNumber != null) {
            if (existing.getCounterpartyAccountNumber() == null
                    || !existing.getCounterpartyAccountNumber().equals(expectedCounterpartyAccountNumber)) {
                throw new CoreException(ErrorCode.IDEMPOTENCY_CONFLICT, "idempotency key request mismatch(counterparty)");
            }
        }
    }
}
