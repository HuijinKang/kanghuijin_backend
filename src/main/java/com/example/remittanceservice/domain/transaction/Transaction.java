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
                        name = "uk_transfer_route_idempotency_key",
                        columnNames = {"transferRoute", "idempotencyKey"}
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
    @Column(nullable = false)
    private TransferRoute transferRoute;

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
            TransferRoute transferRoute,
            String idempotencyKey,
            Account account,
            TransactionType type,
            TransactionStatus status,
            long amount,
            long fee,
            String counterpartyAccountNumber
    ) {
        this.transactionId = transactionId;
        this.transferRoute = transferRoute == null ? TransferRoute.INTERNAL_CORE : transferRoute;
        this.idempotencyKey = idempotencyKey;
        this.account = account;
        this.type = type;
        this.status = status == null ? TransactionStatus.SUCCESS : status;
        this.amount = amount;
        this.fee = fee;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
    }

    public static Transaction deposit(Account account, long amount) {
        return deposit(account, amount, TransferRoute.INTERNAL_CORE, null);
    }

    public static Transaction deposit(
            Account account,
            long amount,
            TransferRoute transferRoute,
            String idempotencyKey
    ) {
        return new Transaction(
                generateTransactionId(),
                transferRoute,
                idempotencyKey,
                account,
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS,
                amount,
                0L,
                null
        );
    }

    public static Transaction withdraw(
            Account account,
            long amount,
            TransferRoute transferRoute,
            String idempotencyKey
    ) {
        return new Transaction(
                generateTransactionId(),
                transferRoute,
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
            long fee,
            TransferRoute transferRoute,
            String idempotencyKey
    ) {
        return new Transaction(
                generateTransactionId(),
                transferRoute,
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
            long amount,
            TransferRoute transferRoute
    ) {
        return new Transaction(
                generateTransactionId(),
                transferRoute,
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
