package org.example.transaction.service;

import org.example.transaction.domain.*;
import org.example.transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountClient;
    private final BigDecimal maxLimit;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  AccountServiceClient accountClient,
                                  @Value("${transaction.max-limit:10000.00}") BigDecimal maxLimit) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
        this.maxLimit = maxLimit;
    }

    @Override
    public TransactionDTO createTransaction(CreateTransactionRequest request) {
        // 1. Validation : montant strictement positif et plafond configuré
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be strictly positive");
        }
        if (request.getAmount().compareTo(maxLimit) > 0) {
            throw new IllegalArgumentException("Transaction amount " + request.getAmount() 
                    + " exceeds configured limit of " + maxLimit);
        }

        // 2 & 3. Consultation du compte : existence, statut actif, devise
        AccountDTO account = fetchAndValidateAccount(request.getAccountId(), request.getCurrency());

        // 4. Consultation du solde et vérification de provision suffisante
        if (account.getBalance() < request.getAmount().doubleValue()) {
            throw new IllegalStateException("Insufficient funds: Account balance is " + account.getBalance() 
                    + " " + account.getCurrency() + ", required: " + request.getAmount());
        }

        // Enregistrement de la transaction
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency().toUpperCase())
                .merchantCategory(request.getMerchantCategory())
                .country(request.getCountry().toUpperCase())
                .createdAt(Instant.now())
                .build();
        Transaction saved = transactionRepository.save(transaction);

        // Mise à jour du solde du compte après enregistrement (débit)
        accountClient.updateBalance(request.getAccountId(), -request.getAmount().doubleValue(), request.getCurrency());

        return TransactionDTO.fromEntity(saved);
    }

    @Override
    public List<TransactionDTO> executeTransfer(TransferRequest request) {
        // 1. Validation du montant et plafond
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be strictly positive");
        }
        if (request.getAmount().compareTo(maxLimit) > 0) {
            throw new IllegalArgumentException("Transfer amount " + request.getAmount() 
                    + " exceeds limit of " + maxLimit);
        }
        if (request.getSourceAccountId().equals(request.getTargetAccountId())) {
            throw new IllegalArgumentException("Source and target accounts must be different for a transfer");
        }

        // 2 & 3. Vérification des comptes source et cible
        AccountDTO sourceAccount = fetchAndValidateAccount(request.getSourceAccountId(), request.getCurrency());
        AccountDTO targetAccount = fetchAndValidateAccount(request.getTargetAccountId(), request.getCurrency());

        // 4. Vérification provision compte source
        if (sourceAccount.getBalance() < request.getAmount().doubleValue()) {
            throw new IllegalStateException("Insufficient funds on source account " + sourceAccount.getId() 
                    + ": Balance=" + sourceAccount.getBalance() + " " + sourceAccount.getCurrency());
        }

        String merchantCat = request.getMerchantCategory() != null ? request.getMerchantCategory() : "TRANSFER";
        String country = request.getCountry() != null ? request.getCountry().toUpperCase() : "MA";

        // Débit du compte source
        Transaction debitTx = Transaction.builder()
                .accountId(request.getSourceAccountId())
                .amount(request.getAmount().negate())
                .currency(request.getCurrency().toUpperCase())
                .merchantCategory(merchantCat)
                .country(country)
                .createdAt(Instant.now())
                .build();
        Transaction savedDebit = transactionRepository.save(debitTx);
        accountClient.updateBalance(request.getSourceAccountId(), -request.getAmount().doubleValue(), request.getCurrency());

        // Crédit du compte cible
        Transaction creditTx = Transaction.builder()
                .accountId(request.getTargetAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency().toUpperCase())
                .merchantCategory(merchantCat)
                .country(country)
                .createdAt(Instant.now())
                .build();
        Transaction savedCredit = transactionRepository.save(creditTx);
        accountClient.updateBalance(request.getTargetAccountId(), request.getAmount().doubleValue(), request.getCurrency());

        return List.of(TransactionDTO.fromEntity(savedDebit), TransactionDTO.fromEntity(savedCredit));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(UUID id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id));
        return TransactionDTO.fromEntity(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByAccountId(UUID accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    private AccountDTO fetchAndValidateAccount(UUID accountId, String expectedCurrency) {
        AccountDTO account;
        try {
            account = accountClient.getAccount(accountId);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Account " + accountId + " not found or inaccessible: " + ex.getMessage());
        }

        if (account == null) {
            throw new IllegalArgumentException("Account " + accountId + " does not exist");
        }
        if (!account.isActive()) {
            throw new IllegalStateException("Account " + accountId + " is not ACTIVE (status=" + account.getStatus() + ")");
        }
        if (!account.getCurrency().equalsIgnoreCase(expectedCurrency)) {
            throw new IllegalArgumentException("Currency mismatch: Account " + accountId + " currency is " 
                    + account.getCurrency() + ", but transaction currency is " + expectedCurrency);
        }
        return account;
    }
}
