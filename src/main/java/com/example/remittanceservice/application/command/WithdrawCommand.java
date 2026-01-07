package com.example.remittanceservice.application.command;

public record WithdrawCommand(
        long accountId,
        long amount
) {
}
