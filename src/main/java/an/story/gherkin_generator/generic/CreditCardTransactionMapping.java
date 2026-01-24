package an.story.gherkin_generator.generic;

import java.util.Arrays;
import java.util.List;

/**
 * Domain mapping for Credit Card Transaction processing.
 *
 * This mapping knows about:
 * - The Transaction REST API endpoint (POST /api/v1/transactions)
 * - The Kafka topic for transaction events (credit-card-transaction-processed)
 * - Common business language patterns and their technical equivalents
 *
 * Example business acceptance criteria:
 *   Given a valid credit card with number ending in 4242
 *   When the transaction is submitted for processing
 *   Then the transaction should be approved
 *
 * Converts to:
 *   Given I have a valid credit card with number ending in "4242"
 *   When I POST to "/api/v1/transactions"
 *   Then the response status should be 200 and the response field "status" should be "APPROVED"
 */
public class CreditCardTransactionMapping extends AbstractDomainMapping {

    public CreditCardTransactionMapping() {
        initializeEndpoints();
        initializeTopics();
        initializeStepPatterns();
        initializeSynonyms();
    }

    private void initializeEndpoints() {
        registerEndpoint(ApiEndpoint.post("/api/v1/transactions", "process transaction"));
        registerEndpoint(ApiEndpoint.get("/api/v1/health", "health check"));
    }

    private void initializeTopics() {
        registerTopic(new KafkaTopic("credit-card-transaction-processed", "CreditCardTransactionProcessed")
                .withRequiredFields("transactionId", "cardNumber", "amount", "status", "processedTimestamp")
                .withOptionalField("authorizationCode")
                .withOptionalField("declineReason"));
    }

    private void initializeStepPatterns() {
        // ===== GIVEN patterns =====

        // Valid card patterns
        registerPattern(StepPattern.given(
                "a valid credit card with number ending in (\\d+)",
                "I have a valid credit card with number ending in \"$1\""
        ).setsField("card_number", "4111111111111$1"));

        registerPattern(StepPattern.given(
                "the card has not expired",
                "the card expiry date is set to next year"
        ));

        registerPattern(StepPattern.given(
                "a valid credit card with sufficient balance",
                "I have a valid credit card with number ending in \"4242\" and the card expiry date is set to next year"
        ));

        // Expired card pattern
        registerPattern(StepPattern.given(
                "a credit card that expired.*",
                "I have a credit card with expiry date in the past"
        ));

        // Invalid card pattern
        registerPattern(StepPattern.given(
                "a credit card with an invalid card number",
                "I have a credit card with number \"123\""
        ));

        // Amount patterns
        registerPattern(StepPattern.given(
                "the transaction amount is \\$?([\\d,]+\\.?\\d*)",
                "I set the transaction amount to $1"
        ).setsField("amount", "$1"));

        // Merchant pattern
        registerPattern(StepPattern.given(
                "the merchant is \"([^\"]+)\"",
                "I set the merchant to \"$1\" with ID \"MERCH-001\""
        ).setsField("merchant_name", "$1"));

        // Generic valid transaction setup
        registerPattern(StepPattern.given(
                "a valid credit card transaction is processed",
                "I have a valid credit card with number ending in \"4242\" and the card expiry date is set to next year and I set the transaction amount to 100.00"
        ));

        // ===== WHEN patterns =====

        // Transaction submission
        registerPattern(StepPattern.when(
                "the transaction is submitted for processing",
                "I POST to \"/api/v1/transactions\""
        ));

        registerPattern(StepPattern.when(
                "the application is processed.*",
                "I POST to \"/api/v1/transactions\""
        ));

        registerPattern(StepPattern.when(
                "I submit the transaction",
                "I POST to \"/api/v1/transactions\""
        ));

        // Kafka consumption
        registerPattern(StepPattern.when(
                "the transaction processed notification is received",
                "I consume the message from topic \"credit-card-transaction-processed\""
        ));

        registerPattern(StepPattern.when(
                "the notification is received",
                "I consume the message from topic \"credit-card-transaction-processed\""
        ));

        // ===== THEN patterns =====

        // Approval patterns
        registerPattern(StepPattern.then(
                "the transaction should be approved",
                "the response status should be 200 and the response field \"status\" should be \"APPROVED\""
        ));

        registerPattern(StepPattern.then(
                "an authorization code should be generated",
                "the response field \"authorization_code\" should not be empty"
        ));

        // Decline patterns
        registerPattern(StepPattern.then(
                "the transaction should be declined",
                "the response status should be 422 and the response field \"status\" should be \"DECLINED\""
        ));

        registerPattern(StepPattern.then(
                "the decline reason should indicate card expiration",
                "the response field \"decline_reason\" should contain \"expired\""
        ));

        registerPattern(StepPattern.then(
                "the decline reason should indicate amount exceeds limit",
                "the response field \"decline_reason\" should contain \"exceeds limit\""
        ));

        registerPattern(StepPattern.then(
                "the decline reason should indicate invalid card number",
                "the response field \"decline_reason\" should contain \"invalid card\""
        ));

        // Kafka notification patterns
        registerPattern(StepPattern.then(
                "a transaction processed notification should be sent",
                "a message should be published to topic \"credit-card-transaction-processed\""
        ));

        registerPattern(StepPattern.then(
                "a transaction processed notification should be sent with declined status",
                "a message should be published to topic \"credit-card-transaction-processed\" with field \"status\" equal to \"DECLINED\""
        ));

        registerPattern(StepPattern.then(
                "the notification should contain the transaction ID and status",
                "the Kafka message field \"transactionId\" should not be empty and the Kafka message field \"status\" should not be empty"
        ));

        registerPattern(StepPattern.then(
                "the notification should contain the masked card number",
                "the Kafka message field \"cardNumber\" should match pattern \"\\\\*{4} \\\\*{4} \\\\*{4} \\\\d{4}\""
        ));

        registerPattern(StepPattern.then(
                "the notification should contain the merchant information",
                "the Kafka message field \"merchantId\" should not be empty and the Kafka message field \"merchantName\" should not be empty"
        ));

        registerPattern(StepPattern.then(
                "the notification should contain the transaction timestamp",
                "the Kafka message field \"processedTimestamp\" should be greater than 0"
        ));

        registerPattern(StepPattern.then(
                "the notification should contain the transaction type",
                "the Kafka message field \"transactionType\" should not be empty"
        ));

        // Event produced patterns (from existing Jira stories)
        registerPattern(StepPattern.then(
                "the \"([^\"]+)\" event is produced",
                "a message should be published to topic \"$1\""
        ));

        registerPattern(StepPattern.then(
                "the \"([^\"]+)\" event is triggered",
                "a message should be published to topic \"$1\""
        ));
    }

    private void initializeSynonyms() {
        registerSynonyms("submit", "process", "send", "submit", "transmitted");
        registerSynonyms("approve", "approved", "success", "successful", "processed");
        registerSynonyms("decline", "declined", "rejected", "fail", "failed");
        registerSynonyms("notification", "event", "message", "sent", "published");
    }

    @Override
    public String getDomainName() {
        return "Credit Card Transaction Processing";
    }

    @Override
    public String getBaseUrlPattern() {
        return "http://localhost:{port}";
    }

    @Override
    public int getDefaultPort() {
        return 8080;
    }

    @Override
    public List<String> getBackgroundSteps() {
        return Arrays.asList(
                "Given the Transaction API is running on port 8080",
                "And a Kafka consumer is listening on topic \"credit-card-transaction-processed\""
        );
    }

    @Override
    public List<String> getStepDefinitionImports() {
        return Arrays.asList(
                "com.fasterxml.jackson.databind.JsonNode",
                "com.fasterxml.jackson.databind.ObjectMapper",
                "java.net.URI",
                "java.net.http.HttpClient",
                "java.net.http.HttpRequest",
                "java.net.http.HttpResponse",
                "java.time.Duration",
                "java.time.LocalDate",
                "java.util.*",
                "org.apache.avro.generic.GenericRecord"
        );
    }

    @Override
    public List<String> getStepDefinitionInstanceVariables() {
        return Arrays.asList(
                "private static final ObjectMapper objectMapper = new ObjectMapper();",
                "private static final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();",
                "",
                "private String baseUrl = \"http://localhost:8080\";",
                "private Map<String, Object> requestData = new HashMap<>();",
                "private HttpResponse<String> lastResponse;",
                "private JsonNode lastResponseBody;",
                "private List<GenericRecord> capturedKafkaMessages = new ArrayList<>();",
                "private GenericRecord lastKafkaMessage;"
        );
    }

    @Override
    public String getBeforeHookCode() {
        return """
                requestData.clear();
                capturedKafkaMessages.clear();
                lastResponse = null;
                lastResponseBody = null;
                lastKafkaMessage = null;

                // Set default values
                requestData.put("transaction_type", "PURCHASE");
                requestData.put("currency", "USD");
                """;
    }

    @Override
    public String getAfterHookCode() {
        return """
                // Cleanup if needed
                """;
    }
}
