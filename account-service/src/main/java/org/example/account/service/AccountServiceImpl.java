package org.example.account.service;

import org.example.account.domain.Account;
import org.example.account.domain.AccountDTO;
import org.example.account.domain.AccountStatus;
import org.example.account.domain.CreateAccountRequest;
import org.example.account.domain.UpdateBalanceRequest;
import org.example.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public AccountDTO createAccount(CreateAccountRequest request) {
        Account account = Account.builder()
                .customerReference(request.getCustomerReference())
                .currency(request.getCurrency().toUpperCase())
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : 0.0)
                .status(request.getStatus() != null ? request.getStatus() : AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.save(account);
        return AccountDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO getAccountById(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        return AccountDTO.fromEntity(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(AccountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> getAccountsByCustomer(String customerReference) {
        return accountRepository.findByCustomerReference(customerReference).stream()
                .map(AccountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDTO updateBalance(UUID id, UpdateBalanceRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));

        if (!account.isActive()) {
            throw new IllegalStateException("Account " + id + " is not ACTIVE (current status: " + account.getStatus() + ")");
        }

        if (!account.getCurrency().equalsIgnoreCase(request.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch: Account currency is " 
                    + account.getCurrency() + ", but requested transaction currency is " + request.getCurrency());
        }

        double newBalance = account.getBalance() + request.getAmount();
        if (newBalance < 0) {
            throw new IllegalStateException("Insufficient funds: current balance is " + account.getBalance() 
                    + " " + account.getCurrency() + ", attempted deduction: " + Math.abs(request.getAmount()));
        }

        account.setBalance(newBalance);
        Account updated = accountRepository.save(account);
        return AccountDTO.fromEntity(updated);
    }

    @Override
    public AccountDTO blockAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        account.setStatus(AccountStatus.BLOCKED);
        return AccountDTO.fromEntity(accountRepository.save(account));
    }

    @Override
    public AccountDTO activateAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        account.setStatus(AccountStatus.ACTIVE);
        return AccountDTO.fromEntity(accountRepository.save(account));
    }
}
