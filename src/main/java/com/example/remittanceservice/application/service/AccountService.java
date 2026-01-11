package com.example.remittanceservice.application.service;

import com.example.remittanceservice.application.command.CreateAccountCommand;
import com.example.remittanceservice.domain.account.AccountRepository;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import com.example.remittanceservice.domain.account.Account;
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
        log.info("[ACCOUNT_CREATE_START] accountNumber={}, ownerName={}", 
                command.accountNumber(), command.ownerName());
        
        try {
            if (accountRepository.existsByAccountNumber(command.accountNumber())) {
                log.warn("[ACCOUNT_CREATE_DUPLICATE] accountNumber={}", command.accountNumber());
                throw new CoreException(ErrorCode.DUPLICATE_ACCOUNT);
            }

            Account account = Account.create(command.accountNumber(), command.ownerName());
            Account saved = accountRepository.save(account);
            
            log.info("[ACCOUNT_CREATE_SUCCESS] accountId={}, accountNumber={}", 
                    saved.getId(), saved.getAccountNumber());
            return saved;
        } catch (DataIntegrityViolationException e) {
            log.error("[ACCOUNT_CREATE_FAILED] accountNumber={}, error={}", 
                    command.accountNumber(), e.getMessage());
            throw new CoreException(ErrorCode.DUPLICATE_ACCOUNT);
        } catch (Exception e) {
            log.error("[ACCOUNT_CREATE_FAILED] accountNumber={}, error={}", 
                    command.accountNumber(), e.getMessage());
            throw e;
        }
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
}
