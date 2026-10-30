package org.example.transaction.service;

import org.example.transaction.domain.CreateTransactionRequest;
import org.example.transaction.domain.TransactionDTO;
import org.example.transaction.domain.TransferRequest;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    TransactionDTO createTransaction(CreateTransactionRequest request);
    List<TransactionDTO> executeTransfer(TransferRequest request);
    TransactionDTO getTransactionById(UUID id);
    List<TransactionDTO> getTransactionsByAccountId(UUID accountId);
    List<TransactionDTO> getAllTransactions();
}
