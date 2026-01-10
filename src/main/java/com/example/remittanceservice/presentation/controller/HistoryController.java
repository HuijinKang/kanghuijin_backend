package com.example.remittanceservice.presentation.controller;

import com.example.remittanceservice.application.facade.HistoryFacade;
import com.example.remittanceservice.presentation.dto.HistoryDto.DepositHistoryResponse;
import com.example.remittanceservice.presentation.dto.HistoryDto.WithdrawHistoryResponse;
import com.example.remittanceservice.presentation.dto.HistoryDto.TransferHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "History", description = "거래내역 조회 API")
public class HistoryController {

    private final HistoryFacade historyFacade;

    @GetMapping("/v1/accounts/{accountId}/deposits")
    @Operation(summary = "입금 내역 조회", description = "지정 계좌의 입금 내역만 최신순으로 조회합니다. (cursor pagination)")
    public ResponseEntity<DepositHistoryResponse> getDeposits(
            @PathVariable long accountId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "50") int limit
    ) {
        DepositHistoryResponse response = historyFacade.getDeposits(accountId, cursor, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/accounts/{accountId}/withdrawals")
    @Operation(summary = "출금 내역 조회", description = "지정 계좌의 출금 내역만 최신순으로 조회합니다. (cursor pagination)")
    public ResponseEntity<WithdrawHistoryResponse> getWithdrawals(
            @PathVariable long accountId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "50") int limit
    ) {
        WithdrawHistoryResponse response = historyFacade.getWithdrawals(accountId, cursor, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/accounts/{accountId}/sent-transfers")
    @Operation(summary = "보낸 이체 내역 조회", description = "지정 계좌에서 보낸 이체 내역만 최신순으로 조회합니다. (cursor pagination)")
    public ResponseEntity<TransferHistoryResponse> getSentTransfers(
            @PathVariable long accountId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "50") int limit
    ) {
        TransferHistoryResponse response = historyFacade.getSentTransfers(accountId, cursor, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/accounts/{accountId}/received-transfers")
    @Operation(summary = "받은 이체 내역 조회", description = "지정 계좌로 받은 이체 내역만 최신순으로 조회합니다. (cursor pagination)")
    public ResponseEntity<TransferHistoryResponse> getReceivedTransfers(
            @PathVariable long accountId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "50") int limit
    ) {
        TransferHistoryResponse response = historyFacade.getReceivedTransfers(accountId, cursor, limit);
        return ResponseEntity.ok(response);
    }
}
