package com.example.remittanceservice.application.command;

public record CreateAccountCommand(
        String ownerName,
        String phoneNumber
) {
    public static CreateAccountCommand of(
            String ownerName,
            String phoneNumber
    ) {
        return new CreateAccountCommand(ownerName, phoneNumber);
    }
}
