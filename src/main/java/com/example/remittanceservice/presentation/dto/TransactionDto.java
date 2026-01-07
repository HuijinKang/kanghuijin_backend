package com.example.remittanceservice.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionDto {

    public record DepositRequest(
            @Positive long amount
    ) {
    }

    public record WithdrawRequest(
            @Positive long amount
    ) {
    }

    public record TransferRequest(
            @NotBlank String fromAccountNumber,
            @NotBlank String toAccountNumber,
            @Positive long amount
    ) {
    }

    public record TransactionResponse(
            long transactionId,
            String status
    ) {
        public static TransactionResponse notImplemented() {
            return new TransactionResponse(0L, "NOT_IMPLEMENTED");
        }
    }
}
