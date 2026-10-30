package org.example.account.web.rest;

import jakarta.validation.Valid;
import org.example.account.domain.AccountDTO;
import org.example.account.domain.CreateAccountRequest;
import org.example.account.domain.UpdateBalanceRequest;
import org.example.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountRestController {

    private final AccountService accountService;

    public AccountRestController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountDTO created = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAccounts(@RequestParam(required = false) String customerReference) {
        if (customerReference != null && !customerReference.isBlank()) {
            return ResponseEntity.ok(accountService.getAccountsByCustomer(customerReference));
        }
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PostMapping("/{id}/balance")
    public ResponseEntity<AccountDTO> updateBalance(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdateBalanceRequest request) {
        return ResponseEntity.ok(accountService.updateBalance(id, request));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<AccountDTO> blockAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.blockAccount(id));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<AccountDTO> activateAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.activateAccount(id));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleNotFound(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleBadRequest(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
