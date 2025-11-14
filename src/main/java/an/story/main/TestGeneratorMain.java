package an.story.main;

import an.story.domain_model.JiraStory;
import an.story.gherkin_generator.GherkinTestGenerator;
import an.story.gherkin_generator.model.TestPackage;
import an.story.parser.JiraStoryParser;

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
        
        JiraStoryParser parser = new JiraStoryParser();
        JiraStory story = parser.parse(storyText);
        
        GherkinTestGenerator generator = new GherkinTestGenerator();
        TestPackage testPackage = generator.generateCompleteTestPackage(story, "an.story.integration_tests");
        
        System.out.println("=== FEATURE FILE ===");
        System.out.println(testPackage.getFeatureFile());
        
        System.out.println("\n=== STEP DEFINITIONS ===");
        System.out.println(testPackage.getStepDefinitions());
    }
}

