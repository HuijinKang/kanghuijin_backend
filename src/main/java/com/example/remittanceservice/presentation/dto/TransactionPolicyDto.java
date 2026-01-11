package com.example.remittanceservice.presentation.dto;

import com.example.remittanceservice.domain.transactionpolicy.TransactionPolicyType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionPolicyDto {

    public record UpsertPolicyRequest(
            @Positive long withdrawDailyLimit,
            @Positive long transferDailyLimit,
            @Positive @Max(10_000) int transferFeeBps
    ) {
    }

    public record PolicyResponse(
            TransactionPolicyType policyType,
            long withdrawDailyLimit,
            long transferDailyLimit,
            int transferFeeBps
    ) {
        public static PolicyResponse of(
                TransactionPolicyType policyType,
                long withdrawDailyLimit,
                long transferDailyLimit,
                int transferFeeBps
        ) {
            return new PolicyResponse(
                    policyType,
                    withdrawDailyLimit,
                    transferDailyLimit,
                    transferFeeBps
            );
        }
    }
}
