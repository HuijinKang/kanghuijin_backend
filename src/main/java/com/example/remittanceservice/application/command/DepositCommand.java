package com.example.remittanceservice.application.command;

public record DepositCommand(
        long accountId,
        long amount
) {
}
