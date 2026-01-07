package com.example.remittanceservice.application.facade;

import com.example.remittanceservice.application.command.DepositCommand;
import com.example.remittanceservice.application.command.TransferCommand;
import com.example.remittanceservice.application.command.WithdrawCommand;
import com.example.remittanceservice.application.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionFacade {

    private final TransactionService transactionService;

    public long deposit(DepositCommand command) {
        return transactionService.deposit(command);
    }

    public long withdraw(WithdrawCommand command) {
        return transactionService.withdraw(command);
    }

    public long transfer(TransferCommand command) {
        return transactionService.transfer(command);
    }
}
