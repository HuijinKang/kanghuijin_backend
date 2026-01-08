package com.example.remittanceservice.application.facade;

import com.example.remittanceservice.application.command.DepositCommand;
import com.example.remittanceservice.application.command.TransferCommand;
import com.example.remittanceservice.application.command.WithdrawCommand;
import com.example.remittanceservice.application.service.PolicyService;
import com.example.remittanceservice.application.service.TransactionService;
import com.example.remittanceservice.domain.policy.PolicyConfig;
import com.example.remittanceservice.domain.policy.PolicyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionFacade {

    private final TransactionService transactionService;
    private final PolicyService policyService;

    public void deposit(DepositCommand command) {
        transactionService.deposit(command);
    }

    public void withdraw(WithdrawCommand command) {
        PolicyConfig policy = policyService.getPolicyConfig(PolicyType.DEFAULT);
        transactionService.withdraw(command, policy.getWithdrawDailyLimit());
    }

    public void transfer(TransferCommand command) {
        PolicyConfig policy = policyService.getPolicyConfig(PolicyType.DEFAULT);
        transactionService.transfer(command, policy.getTransferDailyLimit(), policy.getTransferFeeBps());
    }
}
