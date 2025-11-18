package an.story.main;

import an.story.parser.JiraStoryParser;
import an.story.domain_model.JiraStory;
import an.story.domain_model.Requirement;
import an.story.domain_model.AcceptanceCriterion;
import an.story.domain_model.ServiceTopology;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

// Example Usage and Test

public class JiraStoryParserMain {
    public static void main(String[] args) {
        JiraStoryParser parser = new JiraStoryParser();

        try {
            InputStream resourceStream = JiraStoryParserMain.class.getResourceAsStream("/sample-jira.story");
            if (resourceStream == null) {
                throw new IllegalArgumentException("Story file not found on the classpath: /sample-jira.story");
            }

            String storyText;
            try (resourceStream) {
                storyText = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);
            }
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
