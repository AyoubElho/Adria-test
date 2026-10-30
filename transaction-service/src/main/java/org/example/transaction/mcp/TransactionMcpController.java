package org.example.transaction.mcp;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Standard Model Context Protocol (MCP) JSON-RPC 2.0 Connector for transaction-service.
 * Allows AI assistants, LLMs, and bots to execute and inspect financial transactions.
 */
@RestController
@RequestMapping("/mcp")
public class TransactionMcpController {

    private final TransactionMcpTools mcpTools;

    public TransactionMcpController(TransactionMcpTools mcpTools) {
        this.mcpTools = mcpTools;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> handleJsonRpc(@RequestBody Map<String, Object> request) {
        String method = (String) request.get("method");
        Object id = request.get("id");

        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        try {
            if ("tools/list".equals(method)) {
                response.put("result", Map.of("tools", getToolsDefinitions()));
                return ResponseEntity.ok(response);
            } else if ("tools/call".equals(method)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>) request.get("params");
                String toolName = (String) params.get("name");
                @SuppressWarnings("unchecked")
                Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Collections.emptyMap());

                Object callResult = executeTool(toolName, arguments);
                response.put("result", Map.of(
                        "content", List.of(Map.of(
                                "type", "text",
                                "text", callResult != null ? callResult.toString() : "null"
                        ))
                ));
                return ResponseEntity.ok(response);
            } else {
                response.put("error", Map.of("code", -32601, "message", "Method not found: " + method));
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception ex) {
            response.put("error", Map.of("code", -32000, "message", ex.getMessage()));
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private Object executeTool(String name, Map<String, Object> args) {
        switch (name) {
            case "record_transaction": {
                UUID accountId = UUID.fromString((String) args.get("accountId"));
                BigDecimal amount = new BigDecimal(args.get("amount").toString());
                String currency = (String) args.get("currency");
                String category = (String) args.getOrDefault("merchantCategory", "RETAIL");
                String country = (String) args.getOrDefault("country", "MA");
                return mcpTools.recordTransaction(accountId, amount, currency, category, country);
            }
            case "execute_transfer": {
                UUID sourceAccountId = UUID.fromString((String) args.get("sourceAccountId"));
                UUID targetAccountId = UUID.fromString((String) args.get("targetAccountId"));
                BigDecimal amount = new BigDecimal(args.get("amount").toString());
                String currency = (String) args.get("currency");
                String category = (String) args.getOrDefault("merchantCategory", "TRANSFER");
                String country = (String) args.getOrDefault("country", "MA");
                return mcpTools.executeTransfer(sourceAccountId, targetAccountId, amount, currency, category, country);
            }
            case "get_account_transactions": {
                UUID accountId = UUID.fromString((String) args.get("accountId"));
                return mcpTools.getAccountTransactions(accountId);
            }
            case "get_transaction": {
                UUID transactionId = UUID.fromString((String) args.get("transactionId"));
                return mcpTools.getTransaction(transactionId);
            }
            case "list_all_transactions": {
                return mcpTools.listAllTransactions();
            }
            default:
                throw new IllegalArgumentException("Unknown tool: " + name);
        }
    }

    private List<Map<String, Object>> getToolsDefinitions() {
        return List.of(
                Map.of(
                        "name", "record_transaction",
                        "description", "Record a new payment or debit transaction on an active bank account.",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "accountId", Map.of("type", "string"),
                                        "amount", Map.of("type", "number"),
                                        "currency", Map.of("type", "string"),
                                        "merchantCategory", Map.of("type", "string"),
                                        "country", Map.of("type", "string")
                                ),
                                "required", List.of("accountId", "amount", "currency", "merchantCategory", "country")
                        )
                ),
                Map.of(
                        "name", "execute_transfer",
                        "description", "Transfer funds from a source bank account to a target bank account.",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "sourceAccountId", Map.of("type", "string"),
                                        "targetAccountId", Map.of("type", "string"),
                                        "amount", Map.of("type", "number"),
                                        "currency", Map.of("type", "string"),
                                        "merchantCategory", Map.of("type", "string"),
                                        "country", Map.of("type", "string")
                                ),
                                "required", List.of("sourceAccountId", "targetAccountId", "amount", "currency")
                        )
                ),
                Map.of(
                        "name", "get_account_transactions",
                        "description", "List transaction history for a specific bank account.",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of("accountId", Map.of("type", "string")),
                                "required", List.of("accountId")
                        )
                ),
                Map.of(
                        "name", "get_transaction",
                        "description", "Retrieve transaction details by transaction UUID.",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of("transactionId", Map.of("type", "string")),
                                "required", List.of("transactionId")
                        )
                ),
                Map.of(
                        "name", "list_all_transactions",
                        "description", "List all transactions registered across the platform.",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Collections.emptyMap()
                        )
                )
        );
    }
}
