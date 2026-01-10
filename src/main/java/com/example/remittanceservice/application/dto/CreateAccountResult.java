package com.example.remittanceservice.application.dto;

public record CreateAccountResult(
        long accountId,
        String accountNumber,
        String ownerName
) {
    public static CreateAccountResult of(
            long accountId,
            String accountNumber,
            String ownerName
    ) {
        return new CreateAccountResult(accountId, accountNumber, ownerName);
    }
}
