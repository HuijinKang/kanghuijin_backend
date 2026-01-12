package com.example.remittanceservice.presentation.dto;

import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionHistoryDto {

    public record DepositHistoryResponse(
            List<DepositHistoryItem> items,
            String nextCursor
    ) {
        public static DepositHistoryResponse of(List<DepositHistoryItem> items, String nextCursor) {
            return new DepositHistoryResponse(items, nextCursor);
        }
    }

    public record DepositHistoryItem(
            String transactionId,
            long amount,
            Instant createdAt
    ) {
        public static DepositHistoryItem of(String transactionId, long amount, Instant createdAt) {
            return new DepositHistoryItem(transactionId, amount, createdAt);
        }
    }

    public record WithdrawHistoryResponse(
            List<WithdrawHistoryItem> items,
            String nextCursor
    ) {
        public static WithdrawHistoryResponse of(List<WithdrawHistoryItem> items, String nextCursor) {
            return new WithdrawHistoryResponse(items, nextCursor);
        }
    }

    public record WithdrawHistoryItem(
            String transactionId,
            long amount,
            Instant createdAt
    ) {
        public static WithdrawHistoryItem of(String transactionId, long amount, Instant createdAt) {
            return new WithdrawHistoryItem(transactionId, amount, createdAt);
        }
    }

    public record TransferHistoryResponse(
            List<TransferHistoryItem> items,
            String nextCursor
    ) {
        public static TransferHistoryResponse of(List<TransferHistoryItem> items, String nextCursor) {
            return new TransferHistoryResponse(items, nextCursor);
        }
    }

    public record TransferHistoryItem(
            String transactionId,
            long amount,
            long fee,
            String counterpartyAccountNumber,
            Instant createdAt
    ) {
        public static TransferHistoryItem of(
                String transactionId,
                long amount,
                long fee,
                String counterpartyAccountNumber,
                Instant createdAt
        ) {
            return new TransferHistoryItem(transactionId, amount, fee, counterpartyAccountNumber, createdAt);
        }
    }
}
