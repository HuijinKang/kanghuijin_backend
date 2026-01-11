package com.example.remittanceservice.presentation.controller;

import com.example.remittanceservice.application.command.CreateAccountCommand;
import com.example.remittanceservice.application.dto.AccountDetailResult;
import com.example.remittanceservice.application.dto.CreateAccountResult;
import com.example.remittanceservice.application.facade.AccountFacade;
import com.example.remittanceservice.presentation.dto.AccountDto.AccountDetailResponse;
import com.example.remittanceservice.presentation.dto.AccountDto.CreateAccountRequest;
import com.example.remittanceservice.presentation.dto.AccountDto.CreateAccountResponse;
import com.example.remittanceservice.presentation.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Account", description = "계좌 생성/삭제 API")
public class AccountController {

    private final AccountFacade accountFacade;

    @PostMapping("/v1/accounts")
    @Operation(summary = "계좌 생성", description = "새 계좌를 생성합니다.")
    public ResponseEntity<ApiResponse<CreateAccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        CreateAccountResult created = accountFacade.createAccount(
                CreateAccountCommand.of(request.accountNumber(), request.ownerName())
        );

        CreateAccountResponse response = CreateAccountResponse.of(
                created.accountId(),
                created.accountNumber(),
                created.ownerName()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/v1/accounts/{accountId}")
    @Operation(summary = "계좌 상세 조회", description = "계좌의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<AccountDetailResponse>> getAccount(@PathVariable long accountId) {
        AccountDetailResult result = accountFacade.getAccount(accountId);
        AccountDetailResponse response = AccountDetailResponse.of(
                result.accountId(),
                result.accountNumber(),
                result.ownerName(),
                result.balance(),
                result.status()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/v1/accounts/{accountId}")
    @Operation(summary = "계좌 삭제", description = "계좌를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @PathVariable long accountId
    ) {
        accountFacade.deleteAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success("계좌가 삭제되었습니다"));
    }
}
