package com.example.remittanceservice.infrastructure.transaction;

import com.example.remittanceservice.domain.transaction.Transaction;
import com.example.remittanceservice.domain.transaction.TransactionStatus;
import com.example.remittanceservice.domain.transaction.TransactionType;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface TransactionJpaRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId AND t.type = :type ORDER BY t.id DESC")
    List<Transaction> findByAccountIdAndTypeOrderByIdDesc(
            @Param("accountId") long accountId,
            @Param("type") TransactionType type,
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId AND t.type = :type AND t.id < :cursor ORDER BY t.id DESC")
    List<Transaction> findByAccountIdAndTypeAndIdLessThanOrderByIdDesc(
            @Param("accountId") long accountId,
            @Param("type") TransactionType type,
            @Param("cursor") long cursor,
            Pageable pageable
    );

    @Query("""
            select coalesce(sum(t.amount), 0)
            from Transaction t
            where t.account.id = :accountId
              and t.type = :type
              and t.status = :status
              and t.createdAt >= :startInclusive
              and t.createdAt < :endExclusive
            """)
    long sumAmountByAccountIdAndTypeAndStatusAndCreatedAtBetween(
            @Param("accountId") long accountId,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            @Param("startInclusive") ZonedDateTime startInclusive,
            @Param("endExclusive") ZonedDateTime endExclusive
    );
}
