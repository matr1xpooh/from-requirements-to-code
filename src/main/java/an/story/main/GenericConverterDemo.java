package an.story.main;

import an.story.gherkin_generator.generic.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Demonstrates the Generic Jira-to-Gherkin Converter.
 *
 * This converter reads Jira stories with business-oriented acceptance criteria
 * (written in plain English with Given/When/Then) and transforms them into
 * API-aware Gherkin scenarios that can test REST endpoints and verify Kafka messages.
 *
 * The key difference from hardcoded converters:
 * - Uses pluggable DomainMapping to understand the business domain
 * - Maps business language patterns to technical implementations
 * - Generates fully executable step definitions
 * - Can be extended for new domains by implementing DomainMapping
 *
 * Usage:
 *   mvn exec:java -Dexec.mainClass="an.story.main.GenericConverterDemo"
 */
public class GenericConverterDemo {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║       Generic Jira-to-Gherkin Converter Demonstration          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        try {
            runDemo();
        } catch (Exception e) {
            System.err.println("Error during demonstration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runDemo() throws IOException {
        // Step 1: Create the domain mapping for Credit Card Transactions
        System.out.println("Step 1: Creating domain mapping for Credit Card Transactions...\n");

        DomainMapping domainMapping = new CreditCardTransactionMapping();

        System.out.println("  Domain: " + domainMapping.getDomainName());
        System.out.println("  API Endpoints:");
        for (ApiEndpoint endpoint : domainMapping.getApiEndpoints()) {
            System.out.println("    - " + endpoint.getMethod() + " " + endpoint.getPath());
        }
        System.out.println("  Kafka Topics:");
        for (KafkaTopic topic : domainMapping.getKafkaTopics()) {
            System.out.println("    - " + topic.getTopicName() + " (schema: " + topic.getSchemaName() + ")");
        }
        System.out.println();

        // Step 2: Create the converter with options
        System.out.println("Step 2: Initializing the Generic Converter...\n");

        GenericJiraToGherkinConverter.ConversionOptions options =
                GenericJiraToGherkinConverter.ConversionOptions.defaults()
                        .withPackage("an.story.creditcard.steps.generated")
                        .withComments(true);

        GenericJiraToGherkinConverter converter =
                new GenericJiraToGherkinConverter(domainMapping, options);

        // Step 3: Read and convert the Jira story
        System.out.println("Step 3: Converting Jira story to API-aware Gherkin...\n");
        System.out.println("  Reading: /credit-card-transaction.story");

        GenericJiraToGherkinConverter.ConversionResult result =
                converter.convertFromResource("/credit-card-transaction.story");

        // Step 4: Display the converted feature file
        System.out.println("\n" + "═".repeat(70));
        System.out.println("GENERATED FEATURE FILE:");
        System.out.println("═".repeat(70) + "\n");
        System.out.println(result.getFeatureFile());

        // Step 5: Show any warnings
        if (result.hasWarnings()) {
            System.out.println("\n" + "─".repeat(70));
            System.out.println("CONVERSION WARNINGS:");
            System.out.println("─".repeat(70));
            for (String warning : result.getWarnings()) {
                System.out.println("  ⚠ " + warning);
            }
        }

        // Step 6: Generate full step definitions using the specialized generator
        System.out.println("\n" + "═".repeat(70));
        System.out.println("GENERATING STEP DEFINITIONS:");
        System.out.println("═".repeat(70) + "\n");

        StepDefinitionGenerator stepGenerator = new StepDefinitionGenerator(domainMapping);
        String stepDefinitions = stepGenerator.generate(
                "an.story.creditcard.steps.generated",
                "CreditCardTransactionStepDefinitions"
        );

        // Display first 80 lines of step definitions
        String[] lines = stepDefinitions.split("\n");
        int linesToShow = Math.min(80, lines.length);
        for (int i = 0; i < linesToShow; i++) {
            System.out.println(lines[i]);
        }
        if (lines.length > linesToShow) {
            System.out.println("\n  ... (" + (lines.length - linesToShow) + " more lines) ...");
        }

        // Step 7: Write files to output directory
        System.out.println("\n" + "═".repeat(70));
        System.out.println("WRITING OUTPUT FILES:");
        System.out.println("═".repeat(70) + "\n");

        Path outputDir = Paths.get("target/generated-tests");
        Files.createDirectories(outputDir);

        Path featurePath = outputDir.resolve("credit-card-transaction-generated.feature");
        Files.writeString(featurePath, result.getFeatureFile());
        System.out.println("  ✓ Feature file: " + featurePath);

        Path stepsPath = outputDir.resolve("CreditCardTransactionStepDefinitions.java");
        Files.writeString(stepsPath, stepDefinitions);
        System.out.println("  ✓ Step definitions: " + stepsPath);

        // Step 8: Summary
        System.out.println("\n" + "═".repeat(70));
        System.out.println("CONVERSION COMPLETE!");
        System.out.println("═".repeat(70) + "\n");

        System.out.println("Summary:");
        System.out.println("  - Original Jira story scenarios: " +
                result.getOriginalStory().getAcceptanceCriteria().size());
        System.out.println("  - Generated Gherkin scenarios: " +
                result.getOriginalStory().getAcceptanceCriteria().size());
        System.out.println("  - Conversion warnings: " + result.getWarnings().size());
        System.out.println();

        System.out.println("The generated tests will:");
        System.out.println("  1. Make HTTP requests to POST /api/v1/transactions");
        System.out.println("  2. Verify response status codes and body content");
        System.out.println("  3. Verify Kafka messages on 'credit-card-transaction-processed' topic");
        System.out.println();

        System.out.println("To run the tests:");
        System.out.println("  1. Start the Transaction API server:");
        System.out.println("     mvn exec:java -Dexec.mainClass=\"an.story.creditcard.main.TransactionServerMain\"");
        System.out.println();
        System.out.println("  2. Run the Cucumber tests:");
        System.out.println("     mvn test -Dtest=CucumberTestRunner");
        System.out.println();
    }
}
