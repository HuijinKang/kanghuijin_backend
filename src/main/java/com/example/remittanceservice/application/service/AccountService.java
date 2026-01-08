package com.example.remittanceservice.application.service;

import com.example.remittanceservice.application.command.CreateAccountCommand;
import com.example.remittanceservice.domain.account.AccountRepository;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import com.example.remittanceservice.domain.account.Account;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public Account create(CreateAccountCommand command) {
        Optional<Account> existing = accountRepository.findByAccountNumber(command.accountNumber());
        if (existing.isPresent()) {
            throw new CoreException(ErrorCode.DUPLICATE_ACCOUNT);
        }

        try {
            Account account = Account.create(command.accountNumber(), command.ownerName());
            return accountRepository.save(account);
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorCode.DUPLICATE_ACCOUNT);
        }
    }

    @Transactional
    public void delete(long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CoreException(ErrorCode.NOT_FOUND));

        if (account.isClosed()) {
            return;
        }

        account.close();
        accountRepository.save(account);
    }
}
