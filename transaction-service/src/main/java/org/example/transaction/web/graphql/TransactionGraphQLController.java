package org.example.transaction.web.graphql;

import org.example.transaction.domain.CreateTransactionRequest;
import org.example.transaction.domain.TransactionDTO;
import org.example.transaction.domain.TransferRequest;
import org.example.transaction.service.TransactionService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class TransactionGraphQLController {

    private final TransactionService transactionService;

    public TransactionGraphQLController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @QueryMapping
    public TransactionDTO transactionById(@Argument UUID id) {
        return transactionService.getTransactionById(id);
    }

    @QueryMapping
    public List<TransactionDTO> transactionsByAccount(@Argument UUID accountId) {
        return transactionService.getTransactionsByAccountId(accountId);
    }

    @QueryMapping
    public List<TransactionDTO> allTransactions() {
        return transactionService.getAllTransactions();
    }

    @MutationMapping
    public TransactionDTO createTransaction(@Argument CreateTransactionRequest input) {
        return transactionService.createTransaction(input);
    }

    @MutationMapping
    public List<TransactionDTO> executeTransfer(@Argument TransferRequest input) {
        return transactionService.executeTransfer(input);
    }
}
