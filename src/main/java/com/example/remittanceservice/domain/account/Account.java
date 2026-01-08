package com.example.remittanceservice.domain.account;

import com.example.remittanceservice.domain.common.BaseEntity;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String accountNumber;

    @Column(nullable = false, length = 100)
    private String ownerName;

    @Column(nullable = false)
    private long balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    private Account(String accountNumber, String ownerName, long initialBalance, AccountStatus status) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = initialBalance;
        this.status = status == null ? AccountStatus.ACTIVE : status;
    }

    public static Account create(String accountNumber, String ownerName) {
        return new Account(accountNumber, ownerName, 0L, AccountStatus.ACTIVE);
    }

    public void close() {
        this.status = AccountStatus.CLOSED;
        softDelete();
    }

    public boolean isClosed() {
        return this.status == AccountStatus.CLOSED;
    }

    public void deposit(long amount) {
        if (isClosed()) {
            throw new CoreException(ErrorCode.ACCOUNT_CLOSED);
        }
        this.balance += amount;
    }

    public void withdraw(long amount) {
        if (isClosed()) {
            throw new CoreException(ErrorCode.ACCOUNT_CLOSED);
        }
        if (this.balance < amount) {
            throw new CoreException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        this.balance -= amount;
    }
}
