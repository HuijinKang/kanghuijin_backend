package com.example.remittanceservice.application.service;

import com.example.remittanceservice.application.command.CreateAccountCommand;
import com.example.remittanceservice.domain.account.AccountRepository;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import com.example.remittanceservice.domain.account.Account;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private static final int ACCOUNT_NUMBER_LENGTH = 12;
    private static final int ACCOUNT_NUMBER_GENERATION_MAX_ATTEMPTS = 20;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional(readOnly = true)
    public void validateAccountExists(long accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new CoreException(ErrorCode.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Account getById(long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new CoreException(ErrorCode.NOT_FOUND));
    }

    @Transactional
    public Account create(CreateAccountCommand command) {
        log.info("[ACCOUNT_CREATE_START] ownerName={}, phoneNumber={}", 
                command.ownerName(), command.phoneNumber());
        
        for (int attempt = 1; attempt <= ACCOUNT_NUMBER_GENERATION_MAX_ATTEMPTS; attempt++) {
            String accountNumber = generateAccountNumber();
            if (accountRepository.existsByAccountNumber(accountNumber)) {
                log.warn("[ACCOUNT_NUMBER_COLLISION] attempt={}, accountNumber={}", attempt, accountNumber);
                continue;
            }

            try {
                Account account = Account.create(accountNumber, command.ownerName(), command.phoneNumber());
                Account saved = accountRepository.save(account);

                log.info("[ACCOUNT_CREATE_SUCCESS] accountId={}, accountNumber={}", 
                        saved.getId(), saved.getAccountNumber());
                return saved;
            } catch (DataIntegrityViolationException e) {
                // race condition 등으로 유니크 제약에 걸리면 재시도
                log.warn("[ACCOUNT_CREATE_RETRY_ON_CONSTRAINT] attempt={}, accountNumber={}, error={}",
                        attempt, accountNumber, e.getMessage());
            } catch (Exception e) {
                log.error("[ACCOUNT_CREATE_FAILED] attempt={}, accountNumber={}, error={}",
                        attempt, accountNumber, e.getMessage());
                throw e;
            }
        }

        throw new CoreException(ErrorCode.INTERNAL_ERROR, "계좌번호 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }

    @Transactional
    public void delete(long accountId) {
        log.info("[ACCOUNT_DELETE_START] accountId={}", accountId);
        
        try {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new CoreException(ErrorCode.NOT_FOUND));

            if (account.isClosed()) {
                log.info("[ACCOUNT_ALREADY_CLOSED] accountId={}", accountId);
                return;
            }

            account.close();
            accountRepository.save(account);
            
            log.info("[ACCOUNT_DELETE_SUCCESS] accountId={}, accountNumber={}", 
                    accountId, account.getAccountNumber());
        } catch (Exception e) {
            log.error("[ACCOUNT_DELETE_FAILED] accountId={}, error={}", 
                    accountId, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public Account findByIdForUpdate(long accountId) {
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new CoreException(ErrorCode.NOT_FOUND));
    }

    @Transactional
    public Account findByAccountNumberForUpdate(String accountNumber) {
        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new CoreException(ErrorCode.NOT_FOUND));
    }

    @Transactional
    public void deposit(Account account, long amount) {
        account.deposit(amount);
        accountRepository.save(account);
    }

    @Transactional
    public void withdraw(Account account, long amount) {
        account.withdraw(amount);
        accountRepository.save(account);
    }

    @Transactional
    public void transferMoney(Account senderAccount, Account receiverAccount, long amount, long fee) {
        senderAccount.withdraw(amount + fee);
        receiverAccount.deposit(amount);
        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);
    }

    public void validateAccountActive(Account account) {
        if (account.isClosed()) {
            throw new CoreException(ErrorCode.ACCOUNT_CLOSED);
        }
    }

    private static String generateAccountNumber() {
        StringBuilder sb = new StringBuilder(ACCOUNT_NUMBER_LENGTH);
        for (int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
