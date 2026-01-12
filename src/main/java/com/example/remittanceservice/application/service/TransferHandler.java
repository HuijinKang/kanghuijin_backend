package com.example.remittanceservice.application.service;

import com.example.remittanceservice.application.command.TransferCommand;

public interface TransferHandler {
    void handle(TransferCommand command);
}
