package com.example.remittanceservice.application.facade;

import com.example.remittanceservice.application.service.AccountService;
import com.example.remittanceservice.application.dto.TransactionHistoryPage;
import com.example.remittanceservice.application.service.TransactionHistoryService;
import com.example.remittanceservice.presentation.dto.TransactionHistoryDto.DepositHistoryItem;
import com.example.remittanceservice.presentation.dto.TransactionHistoryDto.DepositHistoryResponse;
import com.example.remittanceservice.presentation.dto.TransactionHistoryDto.WithdrawHistoryItem;
import com.example.remittanceservice.presentation.dto.TransactionHistoryDto.WithdrawHistoryResponse;
import com.example.remittanceservice.presentation.dto.TransactionHistoryDto.TransferHistoryItem;
import com.example.remittanceservice.presentation.dto.TransactionHistoryDto.TransferHistoryResponse;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionHistoryFacade {

    private final TransactionHistoryService transactionHistoryService;
    private final AccountService accountService;

    public DepositHistoryResponse getDeposits(long accountId, Long cursorExclusive, int limit) {
        accountService.validateAccountExists(accountId);

        TransactionHistoryPage page = transactionHistoryService.getDepositPage(accountId, cursorExclusive, limit);

        List<DepositHistoryItem> items = page.transactions().stream()
                .map(transaction -> {
                    Instant createdAt = transaction.getCreatedAt() == null ? null : transaction.getCreatedAt().toInstant();
                    return DepositHistoryItem.of(transaction.getId(), transaction.getAmount(), createdAt);
                })
                .toList();

        return DepositHistoryResponse.of(items, page.nextCursor());
    }

    public WithdrawHistoryResponse getWithdrawals(long accountId, Long cursorExclusive, int limit) {
        accountService.validateAccountExists(accountId);

        TransactionHistoryPage page = transactionHistoryService.getWithdrawPage(accountId, cursorExclusive, limit);

        List<WithdrawHistoryItem> items = page.transactions().stream()
                .map(transaction -> {
                    Instant createdAt = transaction.getCreatedAt() == null ? null : transaction.getCreatedAt().toInstant();
                    return WithdrawHistoryItem.of(transaction.getId(), transaction.getAmount(), createdAt);
                })
                .toList();

        return WithdrawHistoryResponse.of(items, page.nextCursor());
    }

    public TransferHistoryResponse getSentTransfers(long accountId, Long cursorExclusive, int limit) {
        accountService.validateAccountExists(accountId);

        TransactionHistoryPage page = transactionHistoryService.getSentTransferPage(accountId, cursorExclusive, limit);

        List<TransferHistoryItem> items = page.transactions().stream()
                .map(transaction -> {
                    Instant createdAt = transaction.getCreatedAt() == null ? null : transaction.getCreatedAt().toInstant();
                    return TransferHistoryItem.of(
                            transaction.getId(),
                            transaction.getAmount(),
                            transaction.getFee(),
                            transaction.getCounterpartyAccountNumber(),
                            createdAt
                    );
                })
                .toList();

        return TransferHistoryResponse.of(items, page.nextCursor());
    }

    public TransferHistoryResponse getReceivedTransfers(long accountId, Long cursorExclusive, int limit) {
        accountService.validateAccountExists(accountId);

        TransactionHistoryPage page = transactionHistoryService.getReceivedTransferPage(accountId, cursorExclusive, limit);

        List<TransferHistoryItem> items = page.transactions().stream()
                .map(transaction -> {
                    Instant createdAt = transaction.getCreatedAt() == null ? null : transaction.getCreatedAt().toInstant();
                    return TransferHistoryItem.of(
                            transaction.getId(),
                            transaction.getAmount(),
                            transaction.getFee(),
                            transaction.getCounterpartyAccountNumber(),
                            createdAt
                    );
                })
                .toList();

        return TransferHistoryResponse.of(items, page.nextCursor());
    }
}
