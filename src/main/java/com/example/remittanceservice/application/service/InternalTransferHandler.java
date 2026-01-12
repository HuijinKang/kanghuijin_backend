package com.example.remittanceservice.application.service;

import com.example.remittanceservice.application.command.TransferCommand;
import org.springframework.stereotype.Service;

@Service
public class InternalTransferHandler implements TransferHandler {

    @Override
    public void handle(TransferCommand command) {
        // 추후 coreBanking 모듈이나 로직 추가 가능
    }
}
