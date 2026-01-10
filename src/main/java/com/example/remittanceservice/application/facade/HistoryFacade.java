package com.example.remittanceservice.application.facade;

import com.example.remittanceservice.application.service.AccountService;
import com.example.remittanceservice.application.dto.HistoryPage;
import com.example.remittanceservice.application.service.HistoryService;
import com.example.remittanceservice.presentation.dto.HistoryDto.DepositHistoryItem;
import com.example.remittanceservice.presentation.dto.HistoryDto.DepositHistoryResponse;
import com.example.remittanceservice.presentation.dto.HistoryDto.WithdrawHistoryItem;
import com.example.remittanceservice.presentation.dto.HistoryDto.WithdrawHistoryResponse;
import com.example.remittanceservice.presentation.dto.HistoryDto.TransferHistoryItem;
import com.example.remittanceservice.presentation.dto.HistoryDto.TransferHistoryResponse;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HistoryFacade {

    private final HistoryService historyService;
    private final AccountService accountService;

    public DepositHistoryResponse getDeposits(long accountId, Long cursorExclusive, int limit) {
        accountService.validateAccountExists(accountId);

        HistoryPage page = historyService.getDepositPage(accountId, cursorExclusive, limit);

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

        HistoryPage page = historyService.getWithdrawPage(accountId, cursorExclusive, limit);

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

        HistoryPage page = historyService.getSentTransferPage(accountId, cursorExclusive, limit);

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

        HistoryPage page = historyService.getReceivedTransferPage(accountId, cursorExclusive, limit);

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
