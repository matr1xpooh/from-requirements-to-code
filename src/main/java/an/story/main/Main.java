package an.story.main;

import an.story.parser.JiraStoryParser;
import an.story.domain_model.JiraStory;
import an.story.domain_model.Requirement;
import an.story.domain_model.AcceptanceCriterion;
import an.story.domain_model.ServiceTopology;

// Example Usage and Test

public class Main {
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
        
        try {
            JiraStory story = parser.parse(storyText);
            
            System.out.println("=== Parsed Story ===");
            System.out.println("\nValue Statement:");
            System.out.println(story.getValueStatement());
            
            System.out.println("\nRequirements:");
            for (Requirement req : story.getRequirements()) {
                System.out.println(req);
                System.out.println("  Services: " + req.getServices());
                System.out.println("  Events: " + req.getEvents());
                System.out.println("  Schemas: " + req.getSchemas());
            }
            
            System.out.println("\nAcceptance Criteria:");
            for (AcceptanceCriterion ac : story.getAcceptanceCriteria()) {
                System.out.println(ac);
                System.out.println("  Given: " + ac.getGivenStatements());
                System.out.println("  When: " + ac.getWhenStatements());
                System.out.println("  Then: " + ac.getThenStatements());
            }
            
            System.out.println("\n=== Service Topology ===");
            ServiceTopology topology = parser.extractTopology(story);
            System.out.println(topology);
            
        } catch (Exception e) {
            System.err.println("Error parsing story: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
