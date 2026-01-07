package com.example.remittanceservice.presentation.controller;

import com.example.remittanceservice.application.facade.HistoryFacade;
import com.example.remittanceservice.presentation.dto.HistoryDto.TransactionHistoryItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryFacade historyFacade;

    @GetMapping("/v1/accounts/{accountId}/transactions")
    public ResponseEntity<List<TransactionHistoryItem>> getTransactions(
            @PathVariable long accountId
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(List.of());
    }
}
