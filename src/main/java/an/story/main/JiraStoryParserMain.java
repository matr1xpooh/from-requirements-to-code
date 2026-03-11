package an.story.main;

import an.story.parser.JiraStoryParser;
import an.story.domain_model.JiraStory;
import an.story.domain_model.Requirement;
import an.story.domain_model.AcceptanceCriterion;
import an.story.domain_model.ServiceTopology;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

// Example Usage and Test

public class JiraStoryParserMain {
    private static final Logger log = LoggerFactory.getLogger(JiraStoryParserMain.class);

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

            log.info("=== Parsed Story ===");
            log.info("\nValue Statement:");
            log.info("{}", story.getValueStatement());

            log.info("\nRequirements:");
            for (Requirement req : story.getRequirements()) {
                log.info("{}", req);
                log.info("  Services: {}", req.getServices());
                log.info("  Events: {}", req.getEvents());
                log.info("  Schemas: {}", req.getSchemas());
            }

            log.info("\nAcceptance Criteria:");
            for (AcceptanceCriterion ac : story.getAcceptanceCriteria()) {
                log.info("{}", ac);
                log.info("  Given: {}", ac.getGivenStatements());
                log.info("  When: {}", ac.getWhenStatements());
                log.info("  Then: {}", ac.getThenStatements());
            }

            log.info("\n=== Service Topology ===");
            ServiceTopology topology = parser.extractTopology(story);
            log.info("{}", topology);

        } catch (Exception e) {
            log.error("Error parsing story: {}", e.getMessage(), e);
        }
    }
}
