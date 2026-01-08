package com.example.remittanceservice.domain.transaction;

import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private long fee;

    @Column(length = 50)
    private String counterpartyAccountNumber;

    private Transaction(
            Account account,
            TransactionType type,
            TransactionStatus status,
            long amount,
            long fee,
            String counterpartyAccountNumber
    ) {
        this.account = account;
        this.type = type;
        this.status = status == null ? TransactionStatus.SUCCESS : status;
        this.amount = amount;
        this.fee = fee;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
    }

    public static Transaction create(
            Account account,
            TransactionType type,
            long amount,
            long fee,
            String counterpartyAccountNumber
    ) {
        return new Transaction(account, type, TransactionStatus.SUCCESS, amount, fee, counterpartyAccountNumber);
    }

    public static Transaction create(
            Account account,
            TransactionType type,
            TransactionStatus status,
            long amount,
            long fee,
            String counterpartyAccountNumber
    ) {
        return new Transaction(account, type, status, amount, fee, counterpartyAccountNumber);
    }
}
