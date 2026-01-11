package com.example.remittanceservice.fixture;

import com.example.remittanceservice.domain.account.Account;
import com.example.remittanceservice.infrastructure.account.AccountJpaRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountFixtures {

    /**
     * 계좌 생성 (계좌번호 지정)
     */
    public static Account createAccount(String accountNumber, String ownerName) {
        return Account.create(accountNumber, ownerName);
    }

    /**
     * 테스트용 계좌 생성 후 저장 (계좌번호 자동 생성)
     */
    public static Account createAndSave(AccountJpaRepository repository, String ownerName) {
        Account account = Account.create(generateAccountNumber(), ownerName);
        return repository.save(account);
    }

    /**
     * 잔액이 있는 계좌 생성 후 저장 (계좌번호 자동 생성)
     */
    public static Account createAndSaveWithBalance(AccountJpaRepository repository, String ownerName, long balance) {
        Account account = Account.create(generateAccountNumber(), ownerName);
        account.deposit(balance);
        return repository.save(account);
    }

    /**
     * 유니크한 12자리 계좌번호 생성
     */
    public static String generateAccountNumber() {
        String s = String.valueOf(Math.abs(System.nanoTime()));
        if (s.length() >= 12) {
            return s.substring(s.length() - 12);
        }
        return "0".repeat(12 - s.length()) + s;
    }
}
