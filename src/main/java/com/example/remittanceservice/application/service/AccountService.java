package com.example.remittanceservice.application.service;

import com.example.remittanceservice.application.command.CreateAccountCommand;
import com.example.remittanceservice.application.port.out.AccountRepository;
import com.example.remittanceservice.domain.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account create(CreateAccountCommand command) {
        throw new UnsupportedOperationException("NOT_IMPLEMENTED");
    }

    public void delete(long accountId) {
        throw new UnsupportedOperationException("NOT_IMPLEMENTED");
    }
}
