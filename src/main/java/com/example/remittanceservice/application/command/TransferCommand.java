package com.example.remittanceservice.application.command;

public record TransferCommand(
        String fromAccountNumber,
        String toAccountNumber,
        long amount
) {
    public static TransferCommand of(
            String fromAccountNumber,
            String toAccountNumber,
            long amount
    ) {
        return new TransferCommand(fromAccountNumber, toAccountNumber, amount);
    }
}
