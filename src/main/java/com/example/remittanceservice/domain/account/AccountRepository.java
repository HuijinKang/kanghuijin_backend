package com.example.remittanceservice.domain.account;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findById(long id);

    Optional<Account> findByIdForUpdate(long id);

    Optional<Account> findByAccountNumberForUpdate(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    Account save(Account account);
}
