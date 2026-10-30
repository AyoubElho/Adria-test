package org.example.transaction.client;

import org.example.transaction.domain.AccountDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.UUID;


@FeignClient(
        name = "account-service",
        url = "${account.service.url:http://localhost:8081}",
        fallbackFactory = AccountFeignClientFallbackFactory.class
)
public interface AccountFeignClient {

    @GetMapping("/api/accounts/{id}")
    AccountDTO getAccountById(@PathVariable("id") UUID id);

    @PostMapping("/api/accounts/{id}/balance")
    AccountDTO updateBalance(@PathVariable("id") UUID id, @RequestBody Map<String, Object> request);
}
