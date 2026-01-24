package an.story.creditcard.steps;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.generic.GenericRecord;

import an.story.creditcard.controller.TransactionController;
import an.story.creditcard.service.TransactionService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

/**
 * Step definitions for credit card transaction API testing.
 * This class is auto-generated from Jira acceptance criteria using ApiAwareGherkinConverter.
 */
public class TransactionStepDefinitions {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private TransactionController controller;
    private String baseUrl;
    private int serverPort = 8080;

    private Map<String, Object> transactionRequest = new HashMap<>();
    private HttpResponse<String> lastResponse;
    private JsonNode lastResponseBody;
    private List<GenericRecord> capturedKafkaMessages = new ArrayList<>();
    private GenericRecord lastKafkaMessage;

    // Test context shared across steps
    private TestContext testContext;

    public TransactionStepDefinitions() {
        this.testContext = TestContext.getInstance();
    }

    @Before
    public void setUp() {
        transactionRequest.clear();
        capturedKafkaMessages.clear();
        lastResponse = null;
        lastResponseBody = null;
        lastKafkaMessage = null;

        // Set default values
        transactionRequest.put("merchant_id", "MERCH-DEFAULT");
        transactionRequest.put("merchant_name", "Default Merchant");
        transactionRequest.put("transaction_type", "PURCHASE");
        transactionRequest.put("currency", "USD");
    }

    @After
    public void tearDown() {
        if (controller != null) {
            controller.stop();
            controller = null;
        }
    }

    // ===== Background Steps =====

    @Given("the Transaction API is running on port {int}")
    public void theTransactionApiIsRunningOnPort(int port) {
        this.serverPort = port;
        this.baseUrl = "http://localhost:" + port;
        // In actual integration tests, the controller would be started here
        // For unit testing, we can mock or use TestContext
        if (testContext.getController() != null) {
            this.controller = testContext.getController();
            this.baseUrl = controller.getBaseUrl();
        }
    }

    @Given("a Kafka consumer is listening on topic {string}")
    public void aKafkaConsumerIsListeningOnTopic(String topic) {
        // In integration tests, this would set up an embedded Kafka consumer
        // The TestContext would provide access to captured messages
        capturedKafkaMessages = testContext.getCapturedMessages(topic);
    }

    // ===== Given Steps - Card Setup =====

    @Given("I have a valid credit card with number ending in {string}")
    public void iHaveValidCreditCardEndingIn(String lastFourDigits) {
        transactionRequest.put("card_number", "4111111111111" + lastFourDigits);
        transactionRequest.put("card_holder_name", "Test User");
        transactionRequest.put("cvv", "123");
    }

    @Given("the card expiry date is set to next year")
    public void theCardExpiryDateIsSetToNextYear() {
        LocalDate nextYear = LocalDate.now().plusYears(1);
        transactionRequest.put("expiry_month", nextYear.getMonthValue());
        transactionRequest.put("expiry_year", nextYear.getYear());
    }

    @Given("I have a credit card with expiry date in the past")
    public void iHaveCreditCardWithExpiryDateInPast() {
        transactionRequest.put("card_number", "4111111111114242");
        transactionRequest.put("card_holder_name", "Test User");
        transactionRequest.put("cvv", "123");
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        transactionRequest.put("expiry_month", lastMonth.getMonthValue());
        transactionRequest.put("expiry_year", lastMonth.getYear());
    }

    @Given("I have a credit card with number {string}")
    public void iHaveCreditCardWithNumber(String cardNumber) {
        transactionRequest.put("card_number", cardNumber);
        transactionRequest.put("card_holder_name", "Test User");
        transactionRequest.put("cvv", "123");
        LocalDate nextYear = LocalDate.now().plusYears(1);
        transactionRequest.put("expiry_month", nextYear.getMonthValue());
        transactionRequest.put("expiry_year", nextYear.getYear());
    }

    @Given("I set the transaction amount to {double}")
    public void iSetTransactionAmountTo(double amount) {
        transactionRequest.put("amount", amount);
    }

    @Given("I set the merchant to {string} with ID {string}")
    public void iSetMerchantTo(String merchantName, String merchantId) {
        transactionRequest.put("merchant_name", merchantName);
        transactionRequest.put("merchant_id", merchantId);
    }

    // ===== When Steps =====

    @When("I POST the transaction to {string}")
    public void iPostTransactionTo(String endpoint) throws IOException, InterruptedException {
        String jsonBody = objectMapper.writeValueAsString(transactionRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        lastResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = lastResponse.body() != null ? lastResponse.body() : "{}";
        lastResponseBody = objectMapper.readTree(responseBody);

        // Capture any Kafka messages that were published
        capturedKafkaMessages = testContext.getCapturedMessages("credit-card-transaction-processed");
    }

    @When("I consume the message from topic {string}")
    public void iConsumeMessageFromTopic(String topic) {
        capturedKafkaMessages = testContext.getCapturedMessages(topic);
        if (!capturedKafkaMessages.isEmpty()) {
            lastKafkaMessage = capturedKafkaMessages.get(capturedKafkaMessages.size() - 1);
        }
    }

    // ===== Then Steps - Response Validation =====

    @Then("the response status code should be {int} and the response field {string} should be {string}")
    public void responseStatusAndFieldShouldBe(int expectedStatus, String field, String expectedValue) {
        assertEquals(expectedStatus, lastResponse.statusCode(),
                "Expected status " + expectedStatus + " but got " + lastResponse.statusCode());
        assertNotNull(lastResponseBody.get(field), "Field '" + field + "' not found in response");
        assertEquals(expectedValue, lastResponseBody.get(field).asText(),
                "Expected field '" + field + "' to be '" + expectedValue + "'");
    }

    @Then("the response field {string} should not be empty")
    public void responseFieldShouldNotBeEmpty(String field) {
        assertNotNull(lastResponseBody.get(field), "Field '" + field + "' not found in response");
        assertFalse(lastResponseBody.get(field).asText().isEmpty(),
                "Field '" + field + "' should not be empty");
    }

    @Then("the response field {string} should contain {string}")
    public void responseFieldShouldContain(String field, String expectedSubstring) {
        assertNotNull(lastResponseBody.get(field), "Field '" + field + "' not found in response");
        String actualValue = lastResponseBody.get(field).asText();
        assertTrue(actualValue.toLowerCase().contains(expectedSubstring.toLowerCase()),
                "Expected '" + actualValue + "' to contain '" + expectedSubstring + "'");
    }

    // ===== Then Steps - Kafka Validation =====

    @Then("a message should be published to topic {string}")
    public void messageShouldBePublishedToTopic(String topic) {
        assertFalse(capturedKafkaMessages.isEmpty(),
                "Expected at least one message to be published to topic '" + topic + "'");
    }

    @Then("a message should be published to topic {string} with field {string} equal to {string}")
    public void messageShouldBePublishedWithField(String topic, String field, String value) {
        assertFalse(capturedKafkaMessages.isEmpty(),
                "Expected at least one message to be published to topic '" + topic + "'");
        GenericRecord message = capturedKafkaMessages.get(capturedKafkaMessages.size() - 1);
        assertNotNull(message.get(field), "Field '" + field + "' not found in Kafka message");
        assertEquals(value, message.get(field).toString(),
                "Expected Kafka message field '" + field + "' to be '" + value + "'");
    }

    @Then("the Kafka message field {string} should not be empty")
    public void kafkaMessageFieldShouldNotBeEmpty(String field) {
        assertNotNull(lastKafkaMessage, "No Kafka message captured");
        assertNotNull(lastKafkaMessage.get(field), "Field '" + field + "' not found in Kafka message");
        assertFalse(lastKafkaMessage.get(field).toString().isEmpty(),
                "Kafka message field '" + field + "' should not be empty");
    }

    @Then("the Kafka message field {string} should not be empty and the Kafka message field {string} should not be empty")
    public void kafkaMessageFieldsShouldNotBeEmpty(String field1, String field2) {
        kafkaMessageFieldShouldNotBeEmpty(field1);
        kafkaMessageFieldShouldNotBeEmpty(field2);
    }

    @Then("the Kafka message field {string} should match {string}")
    public void kafkaMessageFieldShouldMatch(String field, String pattern) {
        assertNotNull(lastKafkaMessage, "No Kafka message captured");
        assertNotNull(lastKafkaMessage.get(field), "Field '" + field + "' not found in Kafka message");
        String value = lastKafkaMessage.get(field).toString();
        assertTrue(value.matches(pattern),
                "Expected Kafka message field '" + field + "' value '" + value + "' to match pattern '" + pattern + "'");
    }

    @Then("the Kafka message field {string} should be greater than {int}")
    public void kafkaMessageFieldShouldBeGreaterThan(String field, int minValue) {
        assertNotNull(lastKafkaMessage, "No Kafka message captured");
        assertNotNull(lastKafkaMessage.get(field), "Field '" + field + "' not found in Kafka message");
        long value = ((Number) lastKafkaMessage.get(field)).longValue();
        assertTrue(value > minValue,
                "Expected Kafka message field '" + field + "' to be greater than " + minValue);
    }
}
