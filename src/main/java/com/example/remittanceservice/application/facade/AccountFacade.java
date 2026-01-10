package com.example.remittanceservice.application.facade;

import com.example.remittanceservice.application.command.CreateAccountCommand;
import com.example.remittanceservice.application.dto.AccountDetailResult;
import com.example.remittanceservice.application.dto.CreateAccountResult;
import com.example.remittanceservice.application.service.AccountService;
import com.example.remittanceservice.domain.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFacade {

    private final AccountService accountService;

    public CreateAccountResult createAccount(CreateAccountCommand command) {
        Account created = accountService.create(command);
        return CreateAccountResult.of(
                created.getId(),
                created.getAccountNumber(),
                created.getOwnerName()
        );
    }

    public AccountDetailResult getAccount(long accountId) {
        Account account = accountService.getById(accountId);
        return AccountDetailResult.of(
                account.getId(),
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getBalance(),
                account.getStatus().name()
        );
    }

    public void deleteAccount(long accountId) {
        accountService.delete(accountId);
    }
}
