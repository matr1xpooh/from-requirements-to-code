package an.story.gherkin_generator.converter;

import an.story.domain_model.AcceptanceCriterion;
import an.story.domain_model.JiraStory;
import an.story.parser.JiraStoryParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts Jira stories with business-oriented acceptance criteria into
 * API-aware Gherkin scenarios that can be executed against REST endpoints
 * and verify Kafka message publication.
 */
public class ApiAwareGherkinConverter {

    private final JiraStoryParser parser;
    private final TransactionStepMappings stepMappings;

    public ApiAwareGherkinConverter() {
        this.parser = new JiraStoryParser();
        this.stepMappings = new TransactionStepMappings();
    }

    /**
     * Read and convert a Jira story from a file path.
     *
     * @param storyPath Path to the .story file
     * @return API-aware Gherkin feature file content
     */
    public String convertFromFile(Path storyPath) throws IOException {
        String storyContent = Files.readString(storyPath, StandardCharsets.UTF_8);
        return convert(storyContent);
    }

    /**
     * Read and convert a Jira story from a classpath resource.
     *
     * @param resourcePath Resource path (e.g., "/credit-card-transaction.story")
     * @return API-aware Gherkin feature file content
     */
    public String convertFromResource(String resourcePath) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            String storyContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return convert(storyContent);
        }
    }

    /**
     * Convert a Jira story string into an API-aware Gherkin feature file.
     *
     * @param storyContent The raw Jira story content
     * @return API-aware Gherkin feature file content
     */
    public String convert(String storyContent) {
        JiraStory story = parser.parse(storyContent);
        return generateApiAwareFeatureFile(story);
    }

    private String generateApiAwareFeatureFile(JiraStory story) {
        StringBuilder feature = new StringBuilder();

        // Feature header with technical context
        feature.append("# Auto-generated from Jira acceptance criteria\n");
        feature.append("# This feature file contains API-aware scenarios for testing the Transaction REST API\n");
        feature.append("# and verifying Kafka message publication\n\n");

        feature.append("Feature: ").append(generateFeatureName(story)).append("\n");
        feature.append("  ").append(story.getValueStatement().toString()).append("\n\n");

        // Background for common setup
        feature.append("  Background: API and Kafka test infrastructure\n");
        feature.append("    Given the Transaction API is running on port 8080\n");
        feature.append("    And a Kafka consumer is listening on topic \"credit-card-transaction-processed\"\n\n");

        // Convert each scenario
        for (AcceptanceCriterion criterion : story.getAcceptanceCriteria()) {
            feature.append(generateApiAwareScenario(criterion));
            feature.append("\n");
        }

        return feature.toString();
    }

    private String generateFeatureName(JiraStory story) {
        String goal = story.getValueStatement().getGoal();
        // Capitalize first letter and clean up
        goal = goal.substring(0, 1).toUpperCase() + goal.substring(1);
        if (goal.startsWith("Ensure that ")) {
            goal = goal.substring("Ensure that ".length());
            goal = goal.substring(0, 1).toUpperCase() + goal.substring(1);
        }
        return goal;
    }

    private String generateApiAwareScenario(AcceptanceCriterion criterion) {
        StringBuilder scenario = new StringBuilder();

        scenario.append("  Scenario: ").append(criterion.getScenarioName()).append("\n");

        // Transform Given statements
        List<String> transformedGivens = new ArrayList<>();
        for (String given : criterion.getGivenStatements()) {
            transformedGivens.add(stepMappings.transformGiven(given));
        }

        // Transform When statements
        List<String> transformedWhens = new ArrayList<>();
        for (String when : criterion.getWhenStatements()) {
            transformedWhens.add(stepMappings.transformWhen(when));
        }

        // Transform Then statements
        List<String> transformedThens = new ArrayList<>();
        for (String then : criterion.getThenStatements()) {
            transformedThens.add(stepMappings.transformThen(then));
        }

        // Output Given statements
        appendTransformedStatements(scenario, transformedGivens, "Given");

        // Output When statements
        appendTransformedStatements(scenario, transformedWhens, "When");

        // Output Then statements
        appendTransformedStatements(scenario, transformedThens, "Then");

        return scenario.toString();
    }

    private void appendTransformedStatements(StringBuilder scenario, List<String> statements, String primaryKeyword) {
        for (int i = 0; i < statements.size(); i++) {
            String keyword = i == 0 ? primaryKeyword : "And";
            scenario.append("    ").append(keyword).append(" ")
                    .append(statements.get(i)).append("\n");
        }
    }

    /**
     * Generate the corresponding step definitions Java class.
     *
     * @param storyContent The raw Jira story content
     * @param packageName The package name for the generated class
     * @return Java source code for step definitions
     */
    public String generateStepDefinitions(String storyContent, String packageName) {
        JiraStory story = parser.parse(storyContent);
        return generateStepDefinitionsClass(story, packageName);
    }

    private String generateStepDefinitionsClass(JiraStory story, String packageName) {
        StringBuilder code = new StringBuilder();

        // Package and imports
        code.append("package ").append(packageName).append(";\n\n");
        code.append("import io.cucumber.java.Before;\n");
        code.append("import io.cucumber.java.After;\n");
        code.append("import io.cucumber.java.en.Given;\n");
        code.append("import io.cucumber.java.en.When;\n");
        code.append("import io.cucumber.java.en.Then;\n");
        code.append("import static org.junit.jupiter.api.Assertions.*;\n\n");
        code.append("import com.fasterxml.jackson.databind.JsonNode;\n");
        code.append("import com.fasterxml.jackson.databind.ObjectMapper;\n");
        code.append("import okhttp3.*;\n");
        code.append("import org.apache.avro.generic.GenericRecord;\n");
        code.append("import org.apache.kafka.clients.consumer.ConsumerRecord;\n");
        code.append("import org.apache.kafka.clients.consumer.ConsumerRecords;\n");
        code.append("import org.apache.kafka.clients.consumer.KafkaConsumer;\n\n");
        code.append("import an.story.creditcard.controller.TransactionController;\n");
        code.append("import an.story.creditcard.service.TransactionService;\n\n");
        code.append("import java.io.IOException;\n");
        code.append("import java.time.Duration;\n");
        code.append("import java.time.LocalDate;\n");
        code.append("import java.util.*;\n");
        code.append("import java.util.concurrent.*;\n\n");

        // Class declaration
        String className = generateClassName(story);
        code.append("public class ").append(className).append(" {\n\n");

        // Instance variables
        code.append("    private static final ObjectMapper objectMapper = new ObjectMapper();\n");
        code.append("    private static final OkHttpClient httpClient = new OkHttpClient();\n\n");
        code.append("    private TransactionController controller;\n");
        code.append("    private String baseUrl;\n");
        code.append("    private int serverPort = 8080;\n\n");
        code.append("    private Map<String, Object> transactionRequest = new HashMap<>();\n");
        code.append("    private Response lastResponse;\n");
        code.append("    private JsonNode lastResponseBody;\n");
        code.append("    private List<GenericRecord> capturedKafkaMessages = new ArrayList<>();\n");
        code.append("    private GenericRecord lastKafkaMessage;\n\n");

        // Background setup methods
        code.append("    @Given(\"the Transaction API is running on port {int}\")\n");
        code.append("    public void theTransactionApiIsRunningOnPort(int port) {\n");
        code.append("        this.serverPort = port;\n");
        code.append("        this.baseUrl = \"http://localhost:\" + port;\n");
        code.append("        // Note: In actual tests, the server would be started in @Before\n");
        code.append("    }\n\n");

        code.append("    @Given(\"a Kafka consumer is listening on topic {string}\")\n");
        code.append("    public void aKafkaConsumerIsListeningOnTopic(String topic) {\n");
        code.append("        // Kafka consumer setup would happen here\n");
        code.append("        // For integration tests, this would use EmbeddedKafkaBroker\n");
        code.append("    }\n\n");

        // Given step definitions for transaction setup
        code.append("    @Given(\"I have a valid credit card with number ending in {string}\")\n");
        code.append("    public void iHaveValidCreditCardEndingIn(String lastFourDigits) {\n");
        code.append("        transactionRequest.put(\"card_number\", \"4111111111111\" + lastFourDigits);\n");
        code.append("        transactionRequest.put(\"card_holder_name\", \"Test User\");\n");
        code.append("        transactionRequest.put(\"cvv\", \"123\");\n");
        code.append("    }\n\n");

        code.append("    @Given(\"the card expiry date is set to next year\")\n");
        code.append("    public void theCardExpiryDateIsSetToNextYear() {\n");
        code.append("        LocalDate nextYear = LocalDate.now().plusYears(1);\n");
        code.append("        transactionRequest.put(\"expiry_month\", nextYear.getMonthValue());\n");
        code.append("        transactionRequest.put(\"expiry_year\", nextYear.getYear());\n");
        code.append("    }\n\n");

        code.append("    @Given(\"I have a credit card with expiry date in the past\")\n");
        code.append("    public void iHaveCreditCardWithExpiryDateInPast() {\n");
        code.append("        transactionRequest.put(\"card_number\", \"4111111111114242\");\n");
        code.append("        transactionRequest.put(\"card_holder_name\", \"Test User\");\n");
        code.append("        transactionRequest.put(\"cvv\", \"123\");\n");
        code.append("        LocalDate lastMonth = LocalDate.now().minusMonths(1);\n");
        code.append("        transactionRequest.put(\"expiry_month\", lastMonth.getMonthValue());\n");
        code.append("        transactionRequest.put(\"expiry_year\", lastMonth.getYear());\n");
        code.append("    }\n\n");

        code.append("    @Given(\"I have a credit card with number {string}\")\n");
        code.append("    public void iHaveCreditCardWithNumber(String cardNumber) {\n");
        code.append("        transactionRequest.put(\"card_number\", cardNumber);\n");
        code.append("        transactionRequest.put(\"card_holder_name\", \"Test User\");\n");
        code.append("        transactionRequest.put(\"cvv\", \"123\");\n");
        code.append("        LocalDate nextYear = LocalDate.now().plusYears(1);\n");
        code.append("        transactionRequest.put(\"expiry_month\", nextYear.getMonthValue());\n");
        code.append("        transactionRequest.put(\"expiry_year\", nextYear.getYear());\n");
        code.append("    }\n\n");

        code.append("    @Given(\"I set the transaction amount to {double}\")\n");
        code.append("    public void iSetTransactionAmountTo(double amount) {\n");
        code.append("        transactionRequest.put(\"amount\", amount);\n");
        code.append("    }\n\n");

        code.append("    @Given(\"I set the merchant to {string} with ID {string}\")\n");
        code.append("    public void iSetMerchantTo(String merchantName, String merchantId) {\n");
        code.append("        transactionRequest.put(\"merchant_name\", merchantName);\n");
        code.append("        transactionRequest.put(\"merchant_id\", merchantId);\n");
        code.append("    }\n\n");

        // When step definitions
        code.append("    @When(\"I POST the transaction to {string}\")\n");
        code.append("    public void iPostTransactionTo(String endpoint) throws IOException {\n");
        code.append("        String jsonBody = objectMapper.writeValueAsString(transactionRequest);\n");
        code.append("        RequestBody body = RequestBody.create(jsonBody, MediaType.parse(\"application/json\"));\n");
        code.append("        Request request = new Request.Builder()\n");
        code.append("            .url(baseUrl + endpoint)\n");
        code.append("            .post(body)\n");
        code.append("            .build();\n");
        code.append("        lastResponse = httpClient.newCall(request).execute();\n");
        code.append("        String responseBody = lastResponse.body() != null ? lastResponse.body().string() : \"{}\";\n");
        code.append("        lastResponseBody = objectMapper.readTree(responseBody);\n");
        code.append("    }\n\n");

        code.append("    @When(\"I consume the message from topic {string}\")\n");
        code.append("    public void iConsumeMessageFromTopic(String topic) {\n");
        code.append("        // In integration tests, this would poll from EmbeddedKafkaBroker\n");
        code.append("        if (!capturedKafkaMessages.isEmpty()) {\n");
        code.append("            lastKafkaMessage = capturedKafkaMessages.get(capturedKafkaMessages.size() - 1);\n");
        code.append("        }\n");
        code.append("    }\n\n");

        // Then step definitions
        code.append("    @Then(\"the response status code should be {int} and the response field {string} should be {string}\")\n");
        code.append("    public void responseStatusAndFieldShouldBe(int expectedStatus, String field, String expectedValue) {\n");
        code.append("        assertEquals(expectedStatus, lastResponse.code());\n");
        code.append("        assertEquals(expectedValue, lastResponseBody.get(field).asText());\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the response field {string} should not be empty\")\n");
        code.append("    public void responseFieldShouldNotBeEmpty(String field) {\n");
        code.append("        assertNotNull(lastResponseBody.get(field));\n");
        code.append("        assertFalse(lastResponseBody.get(field).asText().isEmpty());\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the response field {string} should contain {string}\")\n");
        code.append("    public void responseFieldShouldContain(String field, String expectedSubstring) {\n");
        code.append("        String actualValue = lastResponseBody.get(field).asText();\n");
        code.append("        assertTrue(actualValue.toLowerCase().contains(expectedSubstring.toLowerCase()),\n");
        code.append("            \"Expected '\" + actualValue + \"' to contain '\" + expectedSubstring + \"'\");\n");
        code.append("    }\n\n");

        code.append("    @Then(\"a message should be published to topic {string}\")\n");
        code.append("    public void messageShouldBePublishedToTopic(String topic) {\n");
        code.append("        // In integration tests, verify message was published\n");
        code.append("        assertFalse(capturedKafkaMessages.isEmpty(), \"Expected at least one Kafka message\");\n");
        code.append("    }\n\n");

        code.append("    @Then(\"a message should be published to topic {string} with field {string} equal to {string}\")\n");
        code.append("    public void messageShouldBePublishedWithField(String topic, String field, String value) {\n");
        code.append("        assertFalse(capturedKafkaMessages.isEmpty(), \"Expected at least one Kafka message\");\n");
        code.append("        GenericRecord message = capturedKafkaMessages.get(capturedKafkaMessages.size() - 1);\n");
        code.append("        assertEquals(value, message.get(field).toString());\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the Kafka message field {string} should not be empty\")\n");
        code.append("    public void kafkaMessageFieldShouldNotBeEmpty(String field) {\n");
        code.append("        assertNotNull(lastKafkaMessage);\n");
        code.append("        assertNotNull(lastKafkaMessage.get(field));\n");
        code.append("        assertFalse(lastKafkaMessage.get(field).toString().isEmpty());\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the Kafka message field {string} should match {string}\")\n");
        code.append("    public void kafkaMessageFieldShouldMatch(String field, String pattern) {\n");
        code.append("        assertNotNull(lastKafkaMessage);\n");
        code.append("        String value = lastKafkaMessage.get(field).toString();\n");
        code.append("        assertTrue(value.matches(pattern), \"Expected '\" + value + \"' to match '\" + pattern + \"'\");\n");
        code.append("    }\n\n");

        code.append("    @Then(\"the Kafka message field {string} should be greater than {int}\")\n");
        code.append("    public void kafkaMessageFieldShouldBeGreaterThan(String field, int minValue) {\n");
        code.append("        assertNotNull(lastKafkaMessage);\n");
        code.append("        long value = (Long) lastKafkaMessage.get(field);\n");
        code.append("        assertTrue(value > minValue);\n");
        code.append("    }\n\n");

        code.append("}\n");

        return code.toString();
    }

    private String generateClassName(JiraStory story) {
        String goal = story.getValueStatement().getGoal();
        // Extract key words and create class name
        String[] words = goal.split("\\s+");
        StringBuilder className = new StringBuilder();
        int wordCount = 0;
        for (String word : words) {
            if (wordCount >= 4) break; // Limit to 4 words
            word = word.replaceAll("[^a-zA-Z]", "");
            if (!word.isEmpty()) {
                className.append(word.substring(0, 1).toUpperCase());
                if (word.length() > 1) {
                    className.append(word.substring(1).toLowerCase());
                }
                wordCount++;
            }
        }
        className.append("StepDefinitions");
        return className.toString();
    }
}
