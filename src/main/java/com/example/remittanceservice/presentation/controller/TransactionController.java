package com.example.remittanceservice.presentation.controller;

import com.example.remittanceservice.application.facade.TransactionFacade;
import com.example.remittanceservice.presentation.dto.TransactionDto.DepositRequest;
import com.example.remittanceservice.presentation.dto.TransactionDto.TransactionResponse;
import com.example.remittanceservice.presentation.dto.TransactionDto.TransferRequest;
import com.example.remittanceservice.presentation.dto.TransactionDto.WithdrawRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionFacade transactionFacade;

    @PostMapping("/v1/accounts/{accountId}/deposits")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable long accountId,
            @Valid @RequestBody DepositRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(TransactionResponse.notImplemented());
    }

    @PostMapping("/v1/accounts/{accountId}/withdrawals")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable long accountId,
            @Valid @RequestBody WithdrawRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(TransactionResponse.notImplemented());
    }

    @PostMapping("/v1/transfers")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(TransactionResponse.notImplemented());
    }
}
