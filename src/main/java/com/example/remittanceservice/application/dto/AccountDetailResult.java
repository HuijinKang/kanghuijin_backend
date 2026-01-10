package com.example.remittanceservice.application.dto;

public record AccountDetailResult(
        long accountId,
        String accountNumber,
        String ownerName,
        long balance,
        String status
) {
    public static AccountDetailResult of(
            long accountId,
            String accountNumber,
            String ownerName,
            long balance,
            String status
    ) {
        return new AccountDetailResult(accountId, accountNumber, ownerName, balance, status);
    }
}
