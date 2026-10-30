package org.example.transaction.mcp;

import org.example.transaction.domain.CreateTransactionRequest;
import org.example.transaction.domain.TransactionDTO;
import org.example.transaction.domain.TransferRequest;
import org.example.transaction.service.TransactionService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * MCP Tools for Transaction operations, callable by Spring AI / AI assistants.
 * Reuses the exact same TransactionService business layer.
 */
@Component
public class TransactionMcpTools {

    private final TransactionService transactionService;

    public TransactionMcpTools(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public TransactionDTO recordTransaction(UUID accountId, BigDecimal amount, String currency,
                                           String merchantCategory, String country) {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(accountId)
                .amount(amount)
                .currency(currency)
                .merchantCategory(merchantCategory)
                .country(country)
                .build();
        return transactionService.createTransaction(request);
    }

    public List<TransactionDTO> executeTransfer(UUID sourceAccountId, UUID targetAccountId,
                                                BigDecimal amount, String currency,
                                                String merchantCategory, String country) {
        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(sourceAccountId)
                .targetAccountId(targetAccountId)
                .amount(amount)
                .currency(currency)
                .merchantCategory(merchantCategory != null ? merchantCategory : "TRANSFER")
                .country(country != null ? country : "MA")
                .build();
        return transactionService.executeTransfer(request);
    }

    public List<TransactionDTO> getAccountTransactions(UUID accountId) {
        return transactionService.getTransactionsByAccountId(accountId);
    }

    public TransactionDTO getTransaction(UUID transactionId) {
        return transactionService.getTransactionById(transactionId);
    }

    public List<TransactionDTO> listAllTransactions() {
        return transactionService.getAllTransactions();
    }
}
