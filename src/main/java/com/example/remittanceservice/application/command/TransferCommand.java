package com.example.remittanceservice.application.command;

public record TransferCommand(
        String fromAccountNumber,
        String toAccountNumber,
        long amount
) {
}
