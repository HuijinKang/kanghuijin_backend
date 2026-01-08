package com.example.remittanceservice.presentation.dto;

import com.example.remittanceservice.domain.policy.PolicyType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PolicyDto {

    public record UpsertPolicyRequest(
            @Positive long withdrawDailyLimit,
            @Positive long transferDailyLimit,
            @Positive @Max(10_000) int transferFeeBps
    ) {
    }

    public record PolicyResponse(
            PolicyType policyType,
            long withdrawDailyLimit,
            long transferDailyLimit,
            int transferFeeBps
    ) {
        public static PolicyResponse of(PolicyType policyType, long withdrawDailyLimit, long transferDailyLimit, int transferFeeBps) {
            return new PolicyResponse(policyType, withdrawDailyLimit, transferDailyLimit, transferFeeBps);
        }
    }
}
