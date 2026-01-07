package com.example.remittanceservice.application.port.out;

import com.example.remittanceservice.domain.account.Account;
import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findById(long id);

    Optional<Account> findByAccountNumber(String accountNumber);

    Account save(Account account);

    void deleteById(long id);
}
