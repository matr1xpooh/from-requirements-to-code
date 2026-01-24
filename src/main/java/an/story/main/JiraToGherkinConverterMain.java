package an.story.main;

import an.story.gherkin_generator.converter.ApiAwareGherkinConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main application that reads a Jira story file and generates:
 * 1. An API-aware Gherkin feature file
 * 2. Step definition Java class
 *
 * The generated tests can verify REST API responses and Kafka message publication.
 */
public class JiraToGherkinConverterMain {

    public static void main(String[] args) {
        System.out.println("=== Jira to API-Aware Gherkin Converter ===\n");

        ApiAwareGherkinConverter converter = new ApiAwareGherkinConverter();

        try {
            // Convert the credit card transaction story
            System.out.println("Reading Jira story from: /credit-card-transaction.story");
            String featureFile = converter.convertFromResource("/credit-card-transaction.story");

            System.out.println("\n--- Generated Feature File ---\n");
            System.out.println(featureFile);

            // Generate step definitions
            String storyContent = readResource("/credit-card-transaction.story");
            String stepDefinitions = converter.generateStepDefinitions(
                storyContent,
                "an.story.creditcard.steps"
            );

            System.out.println("\n--- Generated Step Definitions (excerpt) ---\n");
            // Print first 100 lines of step definitions
            String[] lines = stepDefinitions.split("\n");
            for (int i = 0; i < Math.min(50, lines.length); i++) {
                System.out.println(lines[i]);
            }
            System.out.println("... (truncated for display) ...\n");

            // Write to output files
            Path outputDir = Paths.get("target/generated-tests");
            Files.createDirectories(outputDir);

            Path featurePath = outputDir.resolve("credit-card-transaction.feature");
            Files.writeString(featurePath, featureFile);
            System.out.println("Feature file written to: " + featurePath);

            Path stepsPath = outputDir.resolve("TransactionStepDefinitions.java");
            Files.writeString(stepsPath, stepDefinitions);
            System.out.println("Step definitions written to: " + stepsPath);

            System.out.println("\n=== Conversion Complete ===");

        } catch (IOException e) {
            System.err.println("Error during conversion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String readResource(String resourcePath) throws IOException {
        try (var is = JiraToGherkinConverterMain.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return new String(is.readAllBytes());
        }
    }
}
