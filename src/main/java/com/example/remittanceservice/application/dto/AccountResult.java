package com.example.remittanceservice.application.dto;

public record AccountResult(
        long accountId,
        String accountNumber,
        String ownerName
) {
    public static AccountResult of(long accountId, String accountNumber, String ownerName) {
        return new AccountResult(accountId, accountNumber, ownerName);
    }
}
