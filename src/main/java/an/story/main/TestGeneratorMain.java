package an.story.main;

import an.story.domain_model.JiraStory;
import an.story.gherkin_generator.GherkinTestGenerator;
import an.story.parser.JiraStoryParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

/**
 * Example usage of the Gherkin test generator
 */
public class TestGeneratorMain {
    public static void main(String[] args) {
        String storyText = "{panel:title=Value Statement | titleBGColor=#b9d9ed}\n" +
            "As a Chase Bank product owner, I want to ensure that users over age of 120 are not allowed to apply for a credit card, so that we\n" +
            "maintain data integrity and comply with realistic age expectations.\n" +
            "{panel}\n" +
            "{panel:title=Requirements | titleBGColor=#b9d9ed}\n" +
            "1. update the \"data cleanse\" service to validate the applicant's age and produce an error if the age is greater than 120 years\n" +
            "2. ensure that the \"aoaApplicantDataCleansedErrored\" event is triggered for applicants with an age greater than 120\n" +
            "3. update the avro schema for the \"aoaApplicantDataCleansedErrored\" event to include an error message specific to age validation failure\n" +
            "{panel}\n" +
            "{panel:title=Acceptance Criteria | titleBGColor=#b9d9ed}\n" +
            "Scenario: User age is valid\n" +
            "Given the applicant's age is less than or equal to 120 years\n" +
            "When the application is processed by the Data Cleanse service\n" +
            "Then the \"aoaApplicantDataCleansed\" event is produced\n" +
            "{panel}";
        
        try {
            JiraStoryParser parser = new JiraStoryParser();
            JiraStory story = parser.parse(storyText);
            
            GherkinTestGenerator generator = new GherkinTestGenerator();
            String featureFileContent = generator.generateFeatureFileFromStory(story);
            
            // Create output directory
            Path outputDir = Paths.get("target/generated-tests");
            Files.createDirectories(outputDir);
            
            // Write feature file
            Path featureFile = outputDir.resolve("generated.feature");
            Files.writeString(featureFile, featureFileContent, StandardCharsets.UTF_8);
            System.out.println("Feature file written to: " + featureFile.toAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("Error generating test files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

