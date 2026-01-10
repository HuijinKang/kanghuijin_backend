package com.example.remittanceservice.application.command;

public record DepositCommand(
        long accountId,
        long amount
) {
    public static DepositCommand of(
            long accountId,
            long amount
    ) {
        return new DepositCommand(accountId, amount);
    }
}
