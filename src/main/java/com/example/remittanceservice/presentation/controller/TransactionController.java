package com.example.remittanceservice.presentation.controller;

import com.example.remittanceservice.application.facade.TransactionFacade;
import com.example.remittanceservice.application.command.DepositCommand;
import com.example.remittanceservice.application.command.WithdrawCommand;
import com.example.remittanceservice.application.command.TransferCommand;
import com.example.remittanceservice.presentation.dto.ApiResponse;
import com.example.remittanceservice.presentation.dto.TransactionDto.DepositRequest;
import com.example.remittanceservice.presentation.dto.TransactionDto.TransferRequest;
import com.example.remittanceservice.presentation.dto.TransactionDto.WithdrawRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "입금/출금/이체 API")
public class TransactionController {

    private final TransactionFacade transactionFacade;

    @PostMapping("/v1/accounts/{accountId}/deposits")
    @Operation(summary = "입금", description = "특정 계좌에 금액을 입금합니다.")
    public ResponseEntity<ApiResponse<Void>> deposit(
            @PathVariable long accountId,
            @Valid @RequestBody DepositRequest request
    ) {
        transactionFacade.deposit(DepositCommand.of(accountId, request.amount()));
        return ResponseEntity.ok(ApiResponse.success("입금이 완료되었습니다"));
    }

    @PostMapping("/v1/accounts/{accountId}/withdrawals")
    @Operation(summary = "출금", description = "특정 계좌에서 금액을 출금합니다. (일 한도 1,000,000원)")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @PathVariable long accountId,
            @Valid @RequestBody WithdrawRequest request
    ) {
        transactionFacade.withdraw(WithdrawCommand.of(accountId, request.amount()));
        return ResponseEntity.ok(ApiResponse.success("출금이 완료되었습니다"));
    }

    @PostMapping("/v1/transfers")
    @Operation(summary = "이체", description = "다른 계좌로 이체합니다. (수수료 1%, 일 한도 3,000,000원)")
    public ResponseEntity<ApiResponse<Void>> transfer(
            @Valid @RequestBody TransferRequest request
    ) {
        transactionFacade.transfer(
                TransferCommand.of(request.fromAccountNumber(), request.toAccountNumber(), request.amount())
        );
        return ResponseEntity.ok(ApiResponse.success("이체가 완료되었습니다"));
    }
}
