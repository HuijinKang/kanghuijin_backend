package com.example.remittanceservice.application.dto;

import com.example.remittanceservice.domain.transaction.Transaction;
import java.util.List;

public record HistoryPage(
        List<Transaction> transactions,
        Long nextCursor
) {
}
