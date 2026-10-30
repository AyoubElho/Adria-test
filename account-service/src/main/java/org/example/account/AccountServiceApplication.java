package org.example.account;

import org.example.account.domain.AccountStatus;
import org.example.account.domain.CreateAccountRequest;
import org.example.account.service.AccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner initData(AccountService accountService) {
        return args -> {
            accountService.createAccount(CreateAccountRequest.builder()
                    .customerReference("CUST-1001")
                    .currency("EUR")
                    .initialBalance(5000.0)
                    .status(AccountStatus.ACTIVE)
                    .build());

            accountService.createAccount(CreateAccountRequest.builder()
                    .customerReference("CUST-1002")
                    .currency("EUR")
                    .initialBalance(3500.0)
                    .status(AccountStatus.ACTIVE)
                    .build());

            accountService.createAccount(CreateAccountRequest.builder()
                    .customerReference("CUST-1003")
                    .currency("USD")
                    .initialBalance(1000.0)
                    .status(AccountStatus.BLOCKED)
                    .build());

            System.out.println(">>> Account-Service initialized with test banking accounts.");
        };
    }
}
