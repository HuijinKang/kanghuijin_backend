package com.example.remittanceservice.application.service;

import com.example.remittanceservice.application.command.DepositCommand;
import com.example.remittanceservice.application.port.out.AccountRepository;
import com.example.remittanceservice.application.port.out.TransactionRepository;
import com.example.remittanceservice.application.command.TransferCommand;
import com.example.remittanceservice.application.command.WithdrawCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public long deposit(DepositCommand command) {
        throw new UnsupportedOperationException("NOT_IMPLEMENTED");
    }

    public long withdraw(WithdrawCommand command) {
        throw new UnsupportedOperationException("NOT_IMPLEMENTED");
    }

    public long transfer(TransferCommand command) {
        throw new UnsupportedOperationException("NOT_IMPLEMENTED");
    }
}
