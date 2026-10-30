package org.example.account.mcp;

import org.example.account.domain.AccountDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Standard Model Context Protocol (MCP) JSON-RPC 2.0 Connector.
 * Allows AI assistants, LLMs, Telegram/Discord bots, and MCP clients
 * to discover and execute account tools seamlessly.
 */
@RestController
@RequestMapping("/mcp")
public class AccountMcpController {

    private final AccountMcpTools mcpTools;

    public AccountMcpController(AccountMcpTools mcpTools) {
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
            case "get_account": {
                UUID accountId = UUID.fromString((String) args.get("accountId"));
                return mcpTools.getAccount(accountId);
            }
            case "get_customer_accounts": {
                String customerRef = (String) args.get("customerReference");
                return mcpTools.getCustomerAccounts(customerRef);
            }
            case "list_all_accounts": {
                return mcpTools.listAllAccounts();
            }
            case "create_account": {
                String customerRef = (String) args.get("customerReference");
                String currency = (String) args.get("currency");
                Number balanceNum = (Number) args.get("initialBalance");
                Double balance = balanceNum != null ? balanceNum.doubleValue() : 0.0;
                return mcpTools.createAccount(customerRef, currency, balance);
            }
            case "update_balance": {
                UUID accountId = UUID.fromString((String) args.get("accountId"));
                Number amountNum = (Number) args.get("amount");
                String currency = (String) args.get("currency");
                return mcpTools.updateBalance(accountId, amountNum.doubleValue(), currency);
            }
            default:
                throw new IllegalArgumentException("Unknown tool: " + name);
        }
    }

    private List<Map<String, Object>> getToolsDefinitions() {
        return List.of(
                Map.of(
                        "name", "get_account",
                        "description", "Retrieve banking account details and balance by account UUID.",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of("accountId", Map.of("type", "string", "description", "The UUID of the account")),
                                "required", List.of("accountId")
                        )
                ),
                Map.of(
                        "name", "get_customer_accounts",
                        "description", "List all bank accounts belonging to a specific customer reference.",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of("customerReference", Map.of("type", "string", "description", "Customer code or ID")),
                                "required", List.of("customerReference")
                        )
                ),
                Map.of(
                        "name", "list_all_accounts",
                        "description", "List all existing bank accounts in the system.",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Collections.emptyMap()
                        )
                ),
                Map.of(
                        "name", "create_account",
                        "description", "Open a new bank account with customer reference, currency (EUR, USD, MAD), and initial balance.",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "customerReference", Map.of("type", "string"),
                                        "currency", Map.of("type", "string"),
                                        "initialBalance", Map.of("type", "number")
                                ),
                                "required", List.of("customerReference", "currency")
                        )
                ),
                Map.of(
                        "name", "update_balance",
                        "description", "Credit (positive amount) or debit (negative amount) an active bank account.",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "accountId", Map.of("type", "string"),
                                        "amount", Map.of("type", "number"),
                                        "currency", Map.of("type", "string")
                                ),
                                "required", List.of("accountId", "amount", "currency")
                        )
                )
        );
    }
}
