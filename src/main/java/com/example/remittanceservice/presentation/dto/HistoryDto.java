package com.example.remittanceservice.presentation.dto;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HistoryDto {

    public record TransactionHistoryItem(
            long transactionId,
            String type,
            long amount,
            long fee,
            String counterpartyAccountNumber,
            Instant createdAt
    ) {
    }
}
