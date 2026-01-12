package com.example.remittanceservice.application.service;

import com.example.remittanceservice.application.dto.TransactionHistoryPage;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import com.example.remittanceservice.domain.transaction.TransactionRepository;
import com.example.remittanceservice.domain.transaction.Transaction;
import com.example.remittanceservice.domain.transaction.TransactionType;
import java.util.List;
import java.util.Objects;
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
    public TransactionHistoryPage getDepositPage(long accountId, String cursorExclusive, int limit) {
        return getTransactionPageByType(accountId, TransactionType.DEPOSIT, cursorExclusive, limit);
    }

    @Transactional(readOnly = true)
    public TransactionHistoryPage getWithdrawPage(long accountId, String cursorExclusive, int limit) {
        return getTransactionPageByType(accountId, TransactionType.WITHDRAW, cursorExclusive, limit);
    }

    @Transactional(readOnly = true)
    public TransactionHistoryPage getSentTransferPage(long accountId, String cursorExclusive, int limit) {
        return getTransactionPageByType(accountId, TransactionType.TRANSFER_OUT, cursorExclusive, limit);
    }

    @Transactional(readOnly = true)
    public TransactionHistoryPage getReceivedTransferPage(long accountId, String cursorExclusive, int limit) {
        return getTransactionPageByType(accountId, TransactionType.TRANSFER_IN, cursorExclusive, limit);
    }

    private TransactionHistoryPage getTransactionPageByType(
            long accountId,
            TransactionType type,
            String cursorExclusive,
            int limit
    ) {
        int resolvedLimit = resolveLimit(limit);
        validateCursor(cursorExclusive);

        int fetchSize = resolvedLimit + DEFAULT_HAS_MORE_PROBE;
        List<Transaction> fetchedTransactions;
        if (cursorExclusive == null) {
            fetchedTransactions = transactionRepository.findLatestByAccountIdAndType(accountId, type, fetchSize);
        } else {
            Transaction cursorTx = transactionRepository.findByTransactionId(cursorExclusive)
                    .orElseThrow(() -> new CoreException(ErrorCode.VALIDATION_ERROR, "cursor not found"));

            validateCursorTransaction(cursorTx, accountId, type);

            fetchedTransactions = transactionRepository.findLatestByAccountIdAndTypeBeforeCursor(
                    accountId,
                    type,
                    cursorTx.getCreatedAt(),
                    cursorTx.getId(),
                    fetchSize
            );
        }

        return buildPage(fetchedTransactions, resolvedLimit);
    }

    private static void validateCursorTransaction(Transaction cursorTx, long expectedAccountId, TransactionType expectedType) {
        if (cursorTx.getType() != expectedType) {
            throw new CoreException(ErrorCode.VALIDATION_ERROR, "cursor does not match account/type");
        }

        if (cursorTx.getAccount() == null || cursorTx.getAccount().getId() == null) {
            throw new CoreException(ErrorCode.VALIDATION_ERROR, "cursor does not match account/type");
        }

        if (!Objects.equals(cursorTx.getAccount().getId(), expectedAccountId)) {
            throw new CoreException(ErrorCode.VALIDATION_ERROR, "cursor does not match account/type");
        }
    }

    private TransactionHistoryPage buildPage(List<Transaction> fetched, int resolvedLimit) {
        boolean hasMore = fetched.size() > resolvedLimit;
        List<Transaction> page = hasMore ? fetched.subList(0, resolvedLimit) : fetched;

        String nextCursor = null;
        if (hasMore && !page.isEmpty()) {
            Transaction last = page.get(page.size() - 1);
            nextCursor = last.getTransactionId();
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

    private void validateCursor(String cursorExclusive) {
        if (cursorExclusive != null && cursorExclusive.isBlank()) {
            throw new CoreException(ErrorCode.VALIDATION_ERROR, "cursor must not be blank");
        }
    }
}
