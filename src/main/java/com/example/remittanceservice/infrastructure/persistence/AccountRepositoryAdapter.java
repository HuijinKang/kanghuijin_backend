package com.example.remittanceservice.infrastructure.persistence;

import com.example.remittanceservice.application.port.out.AccountRepository;
import com.example.remittanceservice.domain.account.Account;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;

    @Override
    public Optional<Account> findById(long id) {
        return accountJpaRepository.findById(id);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountJpaRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public Account save(Account account) {
        return accountJpaRepository.save(account);
    }

    @Override
    public void deleteById(long id) {
        accountJpaRepository.deleteById(id);
    }
}
