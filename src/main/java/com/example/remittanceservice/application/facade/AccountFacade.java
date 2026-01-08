package com.example.remittanceservice.application.facade;

import com.example.remittanceservice.application.command.CreateAccountCommand;
import com.example.remittanceservice.application.dto.AccountResult;
import com.example.remittanceservice.application.service.AccountService;
import com.example.remittanceservice.domain.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFacade {

    private final AccountService accountService;

    public AccountResult createAccount(CreateAccountCommand command) {
        Account created = accountService.create(command);
        return AccountResult.of(created.getId(), created.getAccountNumber(), created.getOwnerName());
    }

    public void deleteAccount(long accountId) {
        accountService.delete(accountId);
    }
}
