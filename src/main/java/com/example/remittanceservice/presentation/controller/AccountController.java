package com.example.remittanceservice.presentation.controller;

import com.example.remittanceservice.application.facade.AccountFacade;
import com.example.remittanceservice.presentation.dto.AccountDto.CreateAccountRequest;
import com.example.remittanceservice.presentation.dto.AccountDto.CreateAccountResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

    private final AccountFacade accountFacade;

    @PostMapping("/v1/accounts")
    public ResponseEntity<CreateAccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(CreateAccountResponse.notImplemented());
    }

    @DeleteMapping("/v1/accounts/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable long accountId
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
