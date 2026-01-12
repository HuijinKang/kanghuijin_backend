package com.example.remittanceservice.domain.transaction;

import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_request_client_idempotency_key",
                        columnNames = {"requestClient", "idempotencyKey"}
                )
        },
        indexes = {
        @Index(name = "idx_account_type_created", columnList = "account_id, type, created_at"),
        @Index(name = "idx_account_created_desc", columnList = "account_id, created_at DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TransactionRequestClient requestClient;

    @Column(length = 100)
    private String idempotencyKey;

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
            String transactionId,
            TransactionRequestClient requestClient,
            String idempotencyKey,
            Account account,
            TransactionType type,
            TransactionStatus status,
            long amount,
            long fee,
            String counterpartyAccountNumber
    ) {
        this.transactionId = transactionId;
        this.requestClient = requestClient == null ? TransactionRequestClient.UNKNOWN : requestClient;
        this.idempotencyKey = idempotencyKey;
        this.account = account;
        this.type = type;
        this.status = status == null ? TransactionStatus.SUCCESS : status;
        this.amount = amount;
        this.fee = fee;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
    }

    public static Transaction deposit(Account account, long amount) {
        return deposit(account, amount, TransactionRequestClient.UNKNOWN, null);
    }

    public static Transaction deposit(Account account, long amount, TransactionRequestClient requestClient, String idempotencyKey) {
        return new Transaction(
                generateTransactionId(),
                requestClient,
                idempotencyKey,
                account,
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS,
                amount,
                0L,
                null
        );
    }

    public static Transaction withdraw(Account account, long amount) {
        return withdraw(account, amount, TransactionRequestClient.UNKNOWN, null);
    }

    public static Transaction withdraw(Account account, long amount, TransactionRequestClient requestClient, String idempotencyKey) {
        return new Transaction(
                generateTransactionId(),
                requestClient,
                idempotencyKey,
                account,
                TransactionType.WITHDRAW,
                TransactionStatus.SUCCESS,
                amount,
                0L,
                null
        );
    }

    public static Transaction transferOut(
            Account fromAccount,
            String toAccountNumber,
            long amount,
            long fee
    ) {
        return transferOut(fromAccount, toAccountNumber, amount, fee, TransactionRequestClient.UNKNOWN, null);
    }

    public static Transaction transferOut(
            Account fromAccount,
            String toAccountNumber,
            long amount,
            long fee,
            TransactionRequestClient requestClient,
            String idempotencyKey
    ) {
        return new Transaction(
                generateTransactionId(),
                requestClient,
                idempotencyKey,
                fromAccount,
                TransactionType.TRANSFER_OUT,
                TransactionStatus.SUCCESS,
                amount,
                fee,
                toAccountNumber
        );
    }

    public static Transaction transferIn(
            Account toAccount,
            String fromAccountNumber,
            long amount
    ) {
        return transferIn(toAccount, fromAccountNumber, amount, TransactionRequestClient.UNKNOWN);
    }

    public static Transaction transferIn(
            Account toAccount,
            String fromAccountNumber,
            long amount,
            TransactionRequestClient requestClient
    ) {
        return new Transaction(
                generateTransactionId(),
                requestClient,
                null,
                toAccount,
                TransactionType.TRANSFER_IN,
                TransactionStatus.SUCCESS,
                amount,
                0L,
                fromAccountNumber
        );
    }

    private static String generateTransactionId() {
        String datePart = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE);
        return "TRX-" + datePart + "-" + UUID.randomUUID();
    }
}
