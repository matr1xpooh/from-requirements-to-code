package an.story.main;

import an.story.parser.JiraStoryParser;
import an.story.domain_model.JiraStory;
import an.story.domain_model.Requirement;
import an.story.domain_model.AcceptanceCriterion;
import an.story.domain_model.ServiceTopology;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

// Example Usage and Test

public class Main {
    public static void main(String[] args) {
        JiraStoryParser parser = new JiraStoryParser();

        try {
            Path storyPath = Path.of("sample-jira.story");
            if (!Files.exists(storyPath)) {
                throw new IllegalArgumentException("Story file not found: " + storyPath.toAbsolutePath());
            }

            String storyText = Files.readString(storyPath, StandardCharsets.UTF_8);
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
