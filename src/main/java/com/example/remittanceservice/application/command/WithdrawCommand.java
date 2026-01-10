package com.example.remittanceservice.application.command;

public record WithdrawCommand(
        long accountId,
        long amount
) {
    public static WithdrawCommand of(
            long accountId,
            long amount
    ) {
        return new WithdrawCommand(accountId, amount);
    }
}
