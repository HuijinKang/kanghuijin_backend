package com.example.remittanceservice.domain.transactionpolicy;

import com.example.remittanceservice.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction_policies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionPolicy extends BaseEntity {

    @Column(name = "policy_type", nullable = false, unique = true, length = 50)
    @Enumerated(EnumType.STRING)
    private TransactionPolicyType policyType;

    @Column(name = "withdraw_daily_limit", nullable = false)
    private long withdrawDailyLimit;

    @Column(name = "transfer_daily_limit", nullable = false)
    private long transferDailyLimit;

    @Column(name = "transfer_fee_bps", nullable = false)
    private int transferFeeBps;

    private TransactionPolicy(
            TransactionPolicyType policyType,
            long withdrawDailyLimit,
            long transferDailyLimit,
            int transferFeeBps
    ) {
        this.policyType = policyType;
        this.withdrawDailyLimit = withdrawDailyLimit;
        this.transferDailyLimit = transferDailyLimit;
        this.transferFeeBps = transferFeeBps;
    }

    public static TransactionPolicy of(
            TransactionPolicyType policyType,
            long withdrawDailyLimit,
            long transferDailyLimit,
            int transferFeeBps
    ) {
        return new TransactionPolicy(policyType, withdrawDailyLimit, transferDailyLimit, transferFeeBps);
    }

    public void update(long withdrawDailyLimit, long transferDailyLimit, int transferFeeBps) {
        this.withdrawDailyLimit = withdrawDailyLimit;
        this.transferDailyLimit = transferDailyLimit;
        this.transferFeeBps = transferFeeBps;
    }
}
