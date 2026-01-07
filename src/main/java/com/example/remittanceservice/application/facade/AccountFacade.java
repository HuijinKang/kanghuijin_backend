package com.example.remittanceservice.application.facade;

import com.example.remittanceservice.application.command.CreateAccountCommand;
import com.example.remittanceservice.application.service.AccountService;
import com.example.remittanceservice.domain.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFacade {

    private final AccountService accountService;

    public Account createAccount(CreateAccountCommand command) {
        return accountService.create(command);
    }

    public void deleteAccount(long accountId) {
        accountService.delete(accountId);
    }
}
