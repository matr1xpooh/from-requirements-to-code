package an.story.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import an.story.domain_model.JiraStory;
import an.story.domain_model.Requirement;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Requirements Parsing")
public class RequirementTest extends JiraStoryParserTest{
        
        @Test
        @DisplayName("Should parse numbered requirements")
        void shouldParseNumberedRequirements() {
            String storyText = "{panel:title=Value Statement}\n" +
                "As a user, I want something, so that benefit.\n" +
                "{panel}\n" +
                "{panel:title=Requirements}\n" +
                "1. first requirement\n" +
                "2. second requirement\n" +
                "3. third requirement\n" +
                "{panel}";
            
            JiraStory story = parser.parse(storyText);
            
            assertEquals(3, story.getRequirements().size());
            assertEquals(1, story.getRequirements().get(0).getNumber());
            assertEquals("first requirement", story.getRequirements().get(0).getText());
        }
        
        @Test
        @DisplayName("Should handle multi-line requirements")
        void shouldHandleMultiLineRequirements() {
            String storyText = "{panel:title=Value Statement}\n" +
                "As a user, I want something, so that benefit.\n" +
                "{panel}\n" +
                "{panel:title=Requirements}\n" +
                "1. update the \"data cleanse\" service to validate\n" +
                "   the applicant's age and produce an error\n" +
                "2. ensure that event is triggered\n" +
                "{panel}";
            
            JiraStory story = parser.parse(storyText);
            
            assertEquals(2, story.getRequirements().size());
            String requirement = story.getRequirements().get(0).getText();
            assertTrue(requirement.contains("validate"));
            assertTrue(requirement.contains("produce an error"));
        }
        
        @Test
        @DisplayName("Should extract service names from requirements")
        void shouldExtractServiceNames() {
            String storyText = "{panel:title=Value Statement}\n" +
                "As a user, I want something, so that benefit.\n" +
                "{panel}\n" +
                "{panel:title=Requirements}\n" +
                "1. update the \"data cleanse\" service to validate\n" +
                "2. call the \"payment processing\" service\n" +
                "{panel}";  
            
            JiraStory story = parser.parse(storyText);
            
            Requirement requirement = story.getRequirements().get(0);
            assertEquals("data cleanse", requirement.getServices().get(0));
            assertEquals("payment processing", story.getRequirements().get(1).getServices().get(0));
        }
        
        @Test
        @DisplayName("Should extract event names from requirements")
        void shouldExtractEventNames() {
            String storyText = "{panel:title=Value Statement}\n" +
                "As a user, I want something, so that benefit.\n" +
                "{panel}\n" +
                "{panel:title=Requirements}\n" +
                "1. ensure that the \"aoaApplicantDataCleansedErrored\" event is triggered\n" +
                "2. publish the \"paymentCompleted\" event\n" +
                "{panel}";
            
            JiraStory story = parser.parse(storyText);
            
            assertEquals("aoaApplicantDataCleansedErrored", 
                        story.getRequirements().get(0).getEvents().get(0));
            assertEquals("paymentCompleted", 
                        story.getRequirements().get(1).getEvents().get(0));
        }
        
        @Test
        @DisplayName("Should extract schema names from Avro schema mentions")
        void shouldExtractSchemaNames() {
            String storyText = "{panel:title=Value Statement}\n" +
                "As a user, I want something, so that benefit.\n" +
                "{panel}\n" +
                "{panel:title=Requirements}\n" +
                "1. update the avro schema for the \"aoaApplicantDataCleansedErrored\" event\n" +
                "{panel}";
            
            JiraStory story = parser.parse(storyText);
            
            assertEquals("aoaApplicantDataCleansedErrored.avsc", 
                        story.getRequirements().get(0).getSchemas().get(0));
        }
    }
