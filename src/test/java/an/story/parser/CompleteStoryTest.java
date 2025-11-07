package an.story.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import an.story.domain_model.JiraStory;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Complete Story Parsing")
class CompleteStoryTests extends JiraStoryParserTest {

    @Test
    @DisplayName("Should parse complete story with all sections")
    void shouldParseCompleteStory() {
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
        
        JiraStory story = parser.parse(storyText);
        
        assertNotNull(story);
        assertNotNull(story.getValueStatement());
        assertEquals(3, story.getRequirements().size());
        assertEquals(1, story.getAcceptanceCriteria().size());
    }
    
    @Test
    @DisplayName("Should parse story with multiple acceptance criteria")
    void shouldParseMultipleAcceptanceCriteria() {
        String storyText = "{panel:title=Value Statement | titleBGColor=#b9d9ed}\n" +
            "As a user, I want to do something, so that I achieve a goal.\n" +
            "{panel}\n" +
            "{panel:title=Requirements | titleBGColor=#b9d9ed}\n" +
            "1. do something\n" +
            "{panel}\n" +
            "{panel:title=Acceptance Criteria | titleBGColor=#b9d9ed}\n" +
            "Scenario: First scenario\n" +
            "Given a precondition\n" +
            "When an action occurs\n" +
            "Then an outcome happens\n" +
            "\n" +
            "Scenario: Second scenario\n" +
            "Given another precondition\n" +
            "When another action occurs\n" +
            "Then another outcome happens\n" +
            "{panel}";
        
        JiraStory story = parser.parse(storyText);
        
        assertEquals(2, story.getAcceptanceCriteria().size());
        assertEquals("First scenario", story.getAcceptanceCriteria().get(0).getScenarioName());
        assertEquals("Second scenario", story.getAcceptanceCriteria().get(1).getScenarioName());
    }
}

