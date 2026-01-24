package an.story.gherkin_generator.generic;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Generates fully implemented step definitions from domain mappings.
 *
 * This generator produces executable Cucumber step definitions that:
 * - Make HTTP requests to REST APIs
 * - Verify response status codes and body content
 * - Consume and verify Kafka messages
 * - Handle test data setup and teardown
 */
public class StepDefinitionGenerator {

    private final DomainMapping domainMapping;
    private final GeneratorOptions options;

    public StepDefinitionGenerator(DomainMapping domainMapping) {
        this(domainMapping, GeneratorOptions.defaults());
    }

    public StepDefinitionGenerator(DomainMapping domainMapping, GeneratorOptions options) {
        this.domainMapping = domainMapping;
        this.options = options;
    }

    /**
     * Generate a complete step definitions class with implemented methods.
     */
    public String generate(String packageName, String className) {
        StringBuilder code = new StringBuilder();

        // Package and imports
        code.append("package ").append(packageName).append(";\n\n");
        appendImports(code);

        // Class documentation
        code.append("/**\n");
        code.append(" * Step definitions for ").append(domainMapping.getDomainName()).append(".\n");
        code.append(" *\n");
        code.append(" * This class provides Cucumber step implementations for testing:\n");
        for (ApiEndpoint endpoint : domainMapping.getApiEndpoints()) {
            code.append(" * - ").append(endpoint.getMethod()).append(" ")
                .append(endpoint.getPath()).append("\n");
        }
        for (KafkaTopic topic : domainMapping.getKafkaTopics()) {
            code.append(" * - Kafka topic: ").append(topic.getTopicName()).append("\n");
        }
        code.append(" */\n");

        // Class declaration
        code.append("public class ").append(className).append(" {\n\n");

        // Instance variables
        appendInstanceVariables(code);

        // Constructor
        appendConstructor(code, className);

        // Setup and teardown
        appendSetupTeardown(code);

        // Background steps
        appendBackgroundSteps(code);

        // Step implementations organized by type
        appendGivenSteps(code);
        appendWhenSteps(code);
        appendThenSteps(code);

        // Helper methods
        appendHelperMethods(code);

        code.append("}\n");

        return code.toString();
    }

    private void appendImports(StringBuilder code) {
        code.append("import io.cucumber.java.Before;\n");
        code.append("import io.cucumber.java.After;\n");
        code.append("import io.cucumber.java.en.Given;\n");
        code.append("import io.cucumber.java.en.When;\n");
        code.append("import io.cucumber.java.en.Then;\n");
        code.append("import static org.junit.jupiter.api.Assertions.*;\n\n");

        code.append("import com.fasterxml.jackson.databind.JsonNode;\n");
        code.append("import com.fasterxml.jackson.databind.ObjectMapper;\n");
        code.append("import org.apache.avro.generic.GenericRecord;\n\n");

        code.append("import java.io.IOException;\n");
        code.append("import java.net.URI;\n");
        code.append("import java.net.http.HttpClient;\n");
        code.append("import java.net.http.HttpRequest;\n");
        code.append("import java.net.http.HttpResponse;\n");
        code.append("import java.time.Duration;\n");
        code.append("import java.time.LocalDate;\n");
        code.append("import java.util.*;\n");
        code.append("import java.util.concurrent.*;\n\n");
    }

    private void appendInstanceVariables(StringBuilder code) {
        code.append("    // HTTP client and JSON handling\n");
        code.append("    private static final ObjectMapper objectMapper = new ObjectMapper();\n");
        code.append("    private static final HttpClient httpClient = HttpClient.newBuilder()\n");
        code.append("            .connectTimeout(Duration.ofSeconds(10))\n");
        code.append("            .build();\n\n");

        code.append("    // Server configuration\n");
        code.append("    private String baseUrl = \"http://localhost:").append(domainMapping.getDefaultPort()).append("\";\n");
        code.append("    private int serverPort = ").append(domainMapping.getDefaultPort()).append(";\n\n");

        code.append("    // Request/Response state\n");
        code.append("    private Map<String, Object> requestData = new HashMap<>();\n");
        code.append("    private HttpResponse<String> lastResponse;\n");
        code.append("    private JsonNode lastResponseBody;\n\n");

        code.append("    // Kafka message capture\n");
        code.append("    private List<GenericRecord> capturedKafkaMessages = new ArrayList<>();\n");
        code.append("    private GenericRecord lastKafkaMessage;\n");
        code.append("    private Map<String, List<GenericRecord>> messagesByTopic = new ConcurrentHashMap<>();\n\n");
    }

    private void appendConstructor(StringBuilder code, String className) {
        code.append("    public ").append(className).append("() {\n");
        code.append("        // Default constructor\n");
        code.append("    }\n\n");
    }

    private void appendSetupTeardown(StringBuilder code) {
        code.append("    @Before\n");
        code.append("    public void setUp() {\n");
        code.append("        requestData.clear();\n");
        code.append("        capturedKafkaMessages.clear();\n");
        code.append("        messagesByTopic.clear();\n");
        code.append("        lastResponse = null;\n");
        code.append("        lastResponseBody = null;\n");
        code.append("        lastKafkaMessage = null;\n");
        code.append("        \n");
        code.append("        // Set default values\n");
        code.append("        requestData.put(\"transaction_type\", \"PURCHASE\");\n");
        code.append("        requestData.put(\"currency\", \"USD\");\n");
        code.append("    }\n\n");

        code.append("    @After\n");
        code.append("    public void tearDown() {\n");
        code.append("        // Cleanup resources if needed\n");
        code.append("    }\n\n");
    }

    private void appendBackgroundSteps(StringBuilder code) {
        code.append("    // ===== Background Steps =====\n\n");

        code.append("    @Given(\"the Transaction API is running on port {int}\")\n");
        code.append("    public void theApiIsRunningOnPort(int port) {\n");
        code.append("        this.serverPort = port;\n");
        code.append("        this.baseUrl = \"http://localhost:\" + port;\n");
        code.append("    }\n\n");

        code.append("    @Given(\"a Kafka consumer is listening on topic {string}\")\n");
        code.append("    public void aKafkaConsumerIsListeningOnTopic(String topic) {\n");
        code.append("        // Initialize Kafka consumer for the topic\n");
        code.append("        messagesByTopic.putIfAbsent(topic, new ArrayList<>());\n");
        code.append("    }\n\n");
    }

    private void appendGivenSteps(StringBuilder code) {
        code.append("    // ===== Given Steps - Test Data Setup =====\n\n");

        // Credit card setup
        code.append("    @Given(\"I have a valid credit card with number ending in {string}\")\n");
        code.append("    public void iHaveValidCreditCardEndingIn(String lastFourDigits) {\n");
        code.append("        requestData.put(\"card_number\", \"4111111111111\" + lastFourDigits);\n");
        code.append("        requestData.put(\"card_holder_name\", \"Test User\");\n");
        code.append("        requestData.put(\"cvv\", \"123\");\n");
        code.append("    }\n\n");

        code.append("    @Given(\"the card expiry date is set to next year\")\n");
        code.append("    public void theCardExpiryDateIsSetToNextYear() {\n");
        code.append("        LocalDate nextYear = LocalDate.now().plusYears(1);\n");
        code.append("        requestData.put(\"expiry_month\", nextYear.getMonthValue());\n");
        code.append("        requestData.put(\"expiry_year\", nextYear.getYear());\n");
        code.append("    }\n\n");

        code.append("    @Given(\"I have a credit card with expiry date in the past\")\n");
        code.append("    public void iHaveCreditCardWithExpiryDateInPast() {\n");
        code.append("        requestData.put(\"card_number\", \"4111111111114242\");\n");
        code.append("        requestData.put(\"card_holder_name\", \"Test User\");\n");
        code.append("        requestData.put(\"cvv\", \"123\");\n");
        code.append("        LocalDate lastYear = LocalDate.now().minusYears(1);\n");
        code.append("        requestData.put(\"expiry_month\", lastYear.getMonthValue());\n");
        code.append("        requestData.put(\"expiry_year\", lastYear.getYear());\n");
        code.append("    }\n\n");

        code.append("    @Given(\"I have a credit card with number {string}\")\n");
        code.append("    public void iHaveCreditCardWithNumber(String cardNumber) {\n");
        code.append("        requestData.put(\"card_number\", cardNumber);\n");
        code.append("        requestData.put(\"card_holder_name\", \"Test User\");\n");
        code.append("        requestData.put(\"cvv\", \"123\");\n");
        code.append("        LocalDate nextYear = LocalDate.now().plusYears(1);\n");
        code.append("        requestData.put(\"expiry_month\", nextYear.getMonthValue());\n");
        code.append("        requestData.put(\"expiry_year\", nextYear.getYear());\n");
        code.append("    }\n\n");

        code.append("    @Given(\"I set the transaction amount to {double}\")\n");
        code.append("    public void iSetTransactionAmountTo(double amount) {\n");
        code.append("        requestData.put(\"amount\", amount);\n");
        code.append("    }\n\n");

        code.append("    @Given(\"I set the merchant to {string} with ID {string}\")\n");
        code.append("    public void iSetMerchantTo(String merchantName, String merchantId) {\n");
        code.append("        requestData.put(\"merchant_name\", merchantName);\n");
        code.append("        requestData.put(\"merchant_id\", merchantId);\n");
        code.append("    }\n\n");

        // Composite steps
        code.append("    @Given(\"I have a valid credit card with number ending in {string} and the card expiry date is set to next year\")\n");
        code.append("    public void iHaveValidCardWithExpiry(String lastFourDigits) {\n");
        code.append("        iHaveValidCreditCardEndingIn(lastFourDigits);\n");
        code.append("        theCardExpiryDateIsSetToNextYear();\n");
        code.append("    }\n\n");

        code.append("    @Given(\"I have a valid credit card with number ending in {string} and the card expiry date is set to next year and I set the transaction amount to {double}\")\n");
        code.append("    public void iHaveValidCardWithExpiryAndAmount(String lastFourDigits, double amount) {\n");
        code.append("        iHaveValidCreditCardEndingIn(lastFourDigits);\n");
        code.append("        theCardExpiryDateIsSetToNextYear();\n");
        code.append("        iSetTransactionAmountTo(amount);\n");
        code.append("    }\n\n");
    }

    private void appendWhenSteps(StringBuilder code) {
        code.append("    // ===== When Steps - Actions =====\n\n");

        code.append("    @When(\"I POST to {string}\")\n");
        code.append("    public void iPostTo(String endpoint) throws Exception {\n");
        code.append("        String jsonBody = objectMapper.writeValueAsString(requestData);\n");
        code.append("        \n");
        code.append("        HttpRequest request = HttpRequest.newBuilder()\n");
        code.append("                .uri(URI.create(baseUrl + endpoint))\n");
        code.append("                .header(\"Content-Type\", \"application/json\")\n");
        code.append("                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))\n");
        code.append("                .build();\n");
        code.append("        \n");
        code.append("        lastResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());\n");
        code.append("        String responseBody = lastResponse.body() != null ? lastResponse.body() : \"{}\";\n");
        code.append("        lastResponseBody = objectMapper.readTree(responseBody);\n");
        code.append("    }\n\n");

        code.append("    @When(\"I GET {string}\")\n");
        code.append("    public void iGetFrom(String endpoint) throws Exception {\n");
        code.append("        HttpRequest request = HttpRequest.newBuilder()\n");
        code.append("                .uri(URI.create(baseUrl + endpoint))\n");
        code.append("                .header(\"Accept\", \"application/json\")\n");
        code.append("                .GET()\n");
        code.append("                .build();\n");
        code.append("        \n");
        code.append("        lastResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());\n");
        code.append("        String responseBody = lastResponse.body() != null ? lastResponse.body() : \"{}\";\n");
        code.append("        lastResponseBody = objectMapper.readTree(responseBody);\n");
        code.append("    }\n\n");

        code.append("    @When(\"I consume the message from topic {string}\")\n");
        code.append("    public void iConsumeMessageFromTopic(String topic) {\n");
        code.append("        List<GenericRecord> messages = messagesByTopic.getOrDefault(topic, new ArrayList<>());\n");
        code.append("        if (!messages.isEmpty()) {\n");
        code.append("            lastKafkaMessage = messages.get(messages.size() - 1);\n");
        code.append("        }\n");
        code.append("        capturedKafkaMessages = messages;\n");
        code.append("    }\n\n");
    }

    private void appendThenSteps(StringBuilder code) {
        code.append("    // ===== Then Steps - Assertions =====\n\n");

        // Response status and field assertions
        code.append("    @Then(\"the response status should be {int} and the response field {string} should be {string}\")\n");
        code.append("    public void responseStatusAndFieldShouldBe(int expectedStatus, String field, String expectedValue) {\n");
        code.append("        assertEquals(expectedStatus, lastResponse.statusCode(),\n");
        code.append("                \"Expected status \" + expectedStatus + \" but got \" + lastResponse.statusCode());\n");
        code.append("        assertNotNull(lastResponseBody.get(field), \"Field '\" + field + \"' not found in response\");\n");
        code.append("        assertEquals(expectedValue, lastResponseBody.get(field).asText());\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the response status should be {int}\")\n");
        code.append("    public void theResponseStatusShouldBe(int expectedStatus) {\n");
        code.append("        assertEquals(expectedStatus, lastResponse.statusCode());\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the response field {string} should not be empty\")\n");
        code.append("    public void responseFieldShouldNotBeEmpty(String field) {\n");
        code.append("        assertNotNull(lastResponseBody.get(field), \"Field '\" + field + \"' not found\");\n");
        code.append("        assertFalse(lastResponseBody.get(field).asText().isEmpty(),\n");
        code.append("                \"Field '\" + field + \"' should not be empty\");\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the response field {string} should contain {string}\")\n");
        code.append("    public void responseFieldShouldContain(String field, String expectedSubstring) {\n");
        code.append("        assertNotNull(lastResponseBody.get(field), \"Field '\" + field + \"' not found\");\n");
        code.append("        String actualValue = lastResponseBody.get(field).asText().toLowerCase();\n");
        code.append("        assertTrue(actualValue.contains(expectedSubstring.toLowerCase()),\n");
        code.append("                \"Expected '\" + actualValue + \"' to contain '\" + expectedSubstring + \"'\");\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the response field {string} should be {string}\")\n");
        code.append("    public void responseFieldShouldBe(String field, String expectedValue) {\n");
        code.append("        assertNotNull(lastResponseBody.get(field), \"Field '\" + field + \"' not found\");\n");
        code.append("        assertEquals(expectedValue, lastResponseBody.get(field).asText());\n");
        code.append("    }\n\n");

        // Kafka assertions
        code.append("    @Then(\"a message should be published to topic {string}\")\n");
        code.append("    public void messageShouldBePublishedToTopic(String topic) {\n");
        code.append("        List<GenericRecord> messages = messagesByTopic.getOrDefault(topic, capturedKafkaMessages);\n");
        code.append("        assertFalse(messages.isEmpty(),\n");
        code.append("                \"Expected at least one message to be published to topic '\" + topic + \"'\");\n");
        code.append("    }\n\n");

        code.append("    @Then(\"a message should be published to topic {string} with field {string} equal to {string}\")\n");
        code.append("    public void messageShouldBePublishedWithField(String topic, String field, String value) {\n");
        code.append("        List<GenericRecord> messages = messagesByTopic.getOrDefault(topic, capturedKafkaMessages);\n");
        code.append("        assertFalse(messages.isEmpty(),\n");
        code.append("                \"Expected message in topic '\" + topic + \"'\");\n");
        code.append("        GenericRecord message = messages.get(messages.size() - 1);\n");
        code.append("        assertNotNull(message.get(field), \"Field '\" + field + \"' not found in Kafka message\");\n");
        code.append("        assertEquals(value, message.get(field).toString());\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the Kafka message field {string} should not be empty\")\n");
        code.append("    public void kafkaMessageFieldShouldNotBeEmpty(String field) {\n");
        code.append("        assertNotNull(lastKafkaMessage, \"No Kafka message captured\");\n");
        code.append("        assertNotNull(lastKafkaMessage.get(field), \"Field '\" + field + \"' not found\");\n");
        code.append("        assertFalse(lastKafkaMessage.get(field).toString().isEmpty());\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the Kafka message field {string} should not be empty and the Kafka message field {string} should not be empty\")\n");
        code.append("    public void kafkaMessageFieldsShouldNotBeEmpty(String field1, String field2) {\n");
        code.append("        kafkaMessageFieldShouldNotBeEmpty(field1);\n");
        code.append("        kafkaMessageFieldShouldNotBeEmpty(field2);\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the Kafka message field {string} should match pattern {string}\")\n");
        code.append("    public void kafkaMessageFieldShouldMatchPattern(String field, String pattern) {\n");
        code.append("        assertNotNull(lastKafkaMessage, \"No Kafka message captured\");\n");
        code.append("        String value = lastKafkaMessage.get(field).toString();\n");
        code.append("        assertTrue(value.matches(pattern),\n");
        code.append("                \"Expected '\" + value + \"' to match '\" + pattern + \"'\");\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the Kafka message field {string} should be greater than {int}\")\n");
        code.append("    public void kafkaMessageFieldShouldBeGreaterThan(String field, int minValue) {\n");
        code.append("        assertNotNull(lastKafkaMessage, \"No Kafka message captured\");\n");
        code.append("        long value = ((Number) lastKafkaMessage.get(field)).longValue();\n");
        code.append("        assertTrue(value > minValue);\n");
        code.append("    }\n\n");

        // Combined response assertions for success/failure
        code.append("    @Then(\"the response status should be {int} and the response field {string} should indicate success\")\n");
        code.append("    public void responseStatusAndFieldShouldIndicateSuccess(int expectedStatus, String field) {\n");
        code.append("        assertEquals(expectedStatus, lastResponse.statusCode());\n");
        code.append("        String value = lastResponseBody.get(field).asText().toUpperCase();\n");
        code.append("        assertTrue(value.contains(\"APPROVED\") || value.contains(\"SUCCESS\") || value.contains(\"OK\"));\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the response status should be {int} and the response field {string} should indicate failure\")\n");
        code.append("    public void responseStatusAndFieldShouldIndicateFailure(int expectedStatus, String field) {\n");
        code.append("        assertEquals(expectedStatus, lastResponse.statusCode());\n");
        code.append("        String value = lastResponseBody.get(field).asText().toUpperCase();\n");
        code.append("        assertTrue(value.contains(\"DECLINED\") || value.contains(\"FAILED\") || value.contains(\"ERROR\"));\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the response should contain the expected fields\")\n");
        code.append("    public void responseShouldContainExpectedFields() {\n");
        code.append("        assertNotNull(lastResponseBody);\n");
        code.append("        assertTrue(lastResponseBody.size() > 0, \"Response should have fields\");\n");
        code.append("    }\n\n");
    }

    private void appendHelperMethods(StringBuilder code) {
        code.append("    // ===== Helper Methods =====\n\n");

        code.append("    /**\n");
        code.append("     * Register a captured Kafka message for verification.\n");
        code.append("     * Called by the test infrastructure when a message is received.\n");
        code.append("     */\n");
        code.append("    public void captureKafkaMessage(String topic, GenericRecord message) {\n");
        code.append("        messagesByTopic.computeIfAbsent(topic, k -> new ArrayList<>()).add(message);\n");
        code.append("        capturedKafkaMessages.add(message);\n");
        code.append("    }\n\n");

        code.append("    /**\n");
        code.append("     * Get the last HTTP response for custom assertions.\n");
        code.append("     */\n");
        code.append("    public HttpResponse<String> getLastResponse() {\n");
        code.append("        return lastResponse;\n");
        code.append("    }\n\n");

        code.append("    /**\n");
        code.append("     * Get the last response body as JsonNode for custom assertions.\n");
        code.append("     */\n");
        code.append("    public JsonNode getLastResponseBody() {\n");
        code.append("        return lastResponseBody;\n");
        code.append("    }\n\n");

        code.append("    /**\n");
        code.append("     * Set the base URL for the API under test.\n");
        code.append("     */\n");
        code.append("    public void setBaseUrl(String baseUrl) {\n");
        code.append("        this.baseUrl = baseUrl;\n");
        code.append("    }\n\n");
    }

    /**
     * Options for step definition generation.
     */
    public static class GeneratorOptions {
        public boolean generateJavadoc = true;
        public boolean includeHelperMethods = true;

        public static GeneratorOptions defaults() {
            return new GeneratorOptions();
        }
    }
}
