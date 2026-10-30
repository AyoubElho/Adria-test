package org.example.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("account-service-prefixed", r -> r
                        .path("/account-service/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8081"))

                .route("account-service-direct", r -> r
                        .path("/api/accounts/**")
                        .uri("http://localhost:8081"))

                .route("account-service-mcp", r -> r
                        .path("/account-mcp/**")
                        .filters(f -> f.rewritePath("/account-mcp(?<segment>/?.*)", "/mcp${segment}"))
                        .uri("http://localhost:8081"))

                .route("transaction-service-prefixed", r -> r
                        .path("/transaction-service/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8082"))

                .route("transaction-service-direct", r -> r
                        .path("/api/transactions/**")
                        .uri("http://localhost:8082"))

                .route("transaction-service-mcp", r -> r
                        .path("/transaction-mcp/**")
                        .filters(f -> f.rewritePath("/transaction-mcp(?<segment>/?.*)", "/mcp${segment}"))
                        .uri("http://localhost:8082"))

                .build();
    }
}
