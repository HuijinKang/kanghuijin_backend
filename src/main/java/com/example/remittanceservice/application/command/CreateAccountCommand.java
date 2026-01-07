package com.example.remittanceservice.application.command;

public record CreateAccountCommand(
        String accountNumber,
        String ownerName
) {
}
