package com.example.remittanceservice.presentation.dto;

import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HistoryDto {

    // 입금 내역
    public record DepositHistoryResponse(
            List<DepositHistoryItem> items,
            Long nextCursor
    ) {
        public static DepositHistoryResponse of(List<DepositHistoryItem> items, Long nextCursor) {
            return new DepositHistoryResponse(items, nextCursor);
        }
    }

    public record DepositHistoryItem(
            long transactionId,
            long amount,
            Instant createdAt
    ) {
        public static DepositHistoryItem of(long transactionId, long amount, Instant createdAt) {
            return new DepositHistoryItem(transactionId, amount, createdAt);
        }
    }

    // 출금 내역
    public record WithdrawHistoryResponse(
            List<WithdrawHistoryItem> items,
            Long nextCursor
    ) {
        public static WithdrawHistoryResponse of(List<WithdrawHistoryItem> items, Long nextCursor) {
            return new WithdrawHistoryResponse(items, nextCursor);
        }
    }

    public record WithdrawHistoryItem(
            long transactionId,
            long amount,
            Instant createdAt
    ) {
        public static WithdrawHistoryItem of(long transactionId, long amount, Instant createdAt) {
            return new WithdrawHistoryItem(transactionId, amount, createdAt);
        }
    }

    // 이체 내역 (보낸/받은 공통)
    public record TransferHistoryResponse(
            List<TransferHistoryItem> items,
            Long nextCursor
    ) {
        public static TransferHistoryResponse of(List<TransferHistoryItem> items, Long nextCursor) {
            return new TransferHistoryResponse(items, nextCursor);
        }
    }

    public record TransferHistoryItem(
            long transactionId,
            long amount,
            long fee,
            String counterpartyAccountNumber,
            Instant createdAt
    ) {
        public static TransferHistoryItem of(
                long transactionId,
                long amount,
                long fee,
                String counterpartyAccountNumber,
                Instant createdAt
        ) {
            return new TransferHistoryItem(transactionId, amount, fee, counterpartyAccountNumber, createdAt);
        }
    }
}
