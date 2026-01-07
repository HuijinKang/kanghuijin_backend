package com.example.remittanceservice.domain.transaction;

import com.example.remittanceservice.domain.account.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(nullable = false)
    private Instant createdAt;

    private Transaction(
            Account account,
            TransactionType type,
            TransactionStatus status,
            long amount,
            long fee,
            String counterpartyAccountNumber,
            Instant createdAt
    ) {
        this.account = account;
        this.type = type;
        this.status = status == null ? TransactionStatus.SUCCESS : status;
        this.amount = amount;
        this.fee = fee;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
        this.createdAt = createdAt;
    }
}
