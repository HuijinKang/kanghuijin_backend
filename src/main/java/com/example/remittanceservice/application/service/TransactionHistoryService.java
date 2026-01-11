package com.example.remittanceservice.application.service;

import com.example.remittanceservice.application.dto.TransactionHistoryPage;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import com.example.remittanceservice.domain.transaction.TransactionRepository;
import com.example.remittanceservice.domain.transaction.Transaction;
import com.example.remittanceservice.domain.transaction.TransactionType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionHistoryService {

    private static final int DEFAULT_HAS_MORE_PROBE = 1;
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public TransactionHistoryPage getDepositPage(long accountId, Long cursorExclusive, int limit) {
        return getTransactionPageByType(accountId, TransactionType.DEPOSIT, cursorExclusive, limit);
    }

    @Transactional(readOnly = true)
    public TransactionHistoryPage getWithdrawPage(long accountId, Long cursorExclusive, int limit) {
        return getTransactionPageByType(accountId, TransactionType.WITHDRAW, cursorExclusive, limit);
    }

    @Transactional(readOnly = true)
    public TransactionHistoryPage getSentTransferPage(long accountId, Long cursorExclusive, int limit) {
        return getTransactionPageByType(accountId, TransactionType.TRANSFER_OUT, cursorExclusive, limit);
    }

    @Transactional(readOnly = true)
    public TransactionHistoryPage getReceivedTransferPage(long accountId, Long cursorExclusive, int limit) {
        return getTransactionPageByType(accountId, TransactionType.TRANSFER_IN, cursorExclusive, limit);
    }

    private TransactionHistoryPage getTransactionPageByType(
            long accountId,
            TransactionType type,
            Long cursorExclusive,
            int limit
    ) {
        int resolvedLimit = resolveLimit(limit);
        validateCursor(cursorExclusive);

        int fetchSize = resolvedLimit + DEFAULT_HAS_MORE_PROBE;
        List<Transaction> fetched;
        if (cursorExclusive == null) {
            fetched = transactionRepository.findLatestByAccountIdAndType(accountId, type, fetchSize);
        } else {
            fetched = transactionRepository.findLatestByAccountIdAndTypeBeforeId(accountId, type, cursorExclusive, fetchSize);
        }

        return buildPage(fetched, resolvedLimit);
    }

    private TransactionHistoryPage buildPage(List<Transaction> fetched, int resolvedLimit) {
        boolean hasMore = fetched.size() > resolvedLimit;
        List<Transaction> page = hasMore ? fetched.subList(0, resolvedLimit) : fetched;

        Long nextCursor = null;
        if (hasMore && !page.isEmpty()) {
            Transaction last = page.get(page.size() - 1);
            nextCursor = last.getId();
        }

        return new TransactionHistoryPage(page, nextCursor);
    }

    private int resolveLimit(int limit) {
        int resolvedLimit = limit <= 0 ? DEFAULT_LIMIT : limit;
        if (resolvedLimit > MAX_LIMIT) {
            throw new CoreException(ErrorCode.VALIDATION_ERROR, "limit must be between 1 and %d".formatted(MAX_LIMIT));
        }
        return resolvedLimit;
    }

    private void validateCursor(Long cursorExclusive) {
        if (cursorExclusive != null && cursorExclusive < 1) {
            throw new CoreException(ErrorCode.VALIDATION_ERROR, "cursor must be positive");
        }
    }
}
