package com.example.remittanceservice.application.command;

public record CreateAccountCommand(
        String accountNumber,
        String ownerName
) {
    public static CreateAccountCommand of(String accountNumber, String ownerName) {
        return new CreateAccountCommand(accountNumber, ownerName);
    }
}
