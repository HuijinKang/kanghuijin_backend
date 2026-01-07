package com.example.remittanceservice.presentation.controller;

import com.example.remittanceservice.application.facade.HistoryFacade;
import com.example.remittanceservice.presentation.dto.HistoryDto.TransactionHistoryItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "History", description = "거래내역 조회 API")
public class HistoryController {

    private final HistoryFacade historyFacade;

    @GetMapping("/v1/accounts/{accountId}/transactions")
    @Operation(summary = "거래내역 조회", description = "지정 계좌의 송금/수취 내역을 최신순으로 조회합니다.")
    public ResponseEntity<List<TransactionHistoryItem>> getTransactions(
            @PathVariable long accountId
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(List.of());
    }
}
