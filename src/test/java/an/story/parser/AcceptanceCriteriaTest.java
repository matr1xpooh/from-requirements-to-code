package an.story.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import an.story.domain_model.JiraStory;
import an.story.domain_model.AcceptanceCriterion;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Acceptance Criteria Parsing")
class AcceptanceCriteriaTests extends JiraStoryParserTest{
    
    @Test
    @DisplayName("Should parse Gherkin scenario with Given/When/Then")
    void shouldParseGherkinScenario() {
        String storyText = "{panel:title=Value Statement}\n" +
            "As a user, I want something, so that benefit.\n" +
            "{panel}\n" +
            "{panel:title=Acceptance Criteria}\n" +
            "Scenario: User age is valid\n" +
            "Given the applicant's age is less than or equal to 120 years\n" +
            "When the application is processed by the Data Cleanse service\n" +
            "Then the \"aoaApplicantDataCleansed\" event is produced\n" +
            "{panel}";
        
        JiraStory story = parser.parse(storyText);
        AcceptanceCriterion ac = story.getAcceptanceCriteria().get(0);
        
        assertEquals("User age is valid", ac.getScenarioName());
        assertEquals(1, ac.getGivenStatements().size());
        assertEquals(1, ac.getWhenStatements().size());
        assertEquals(1, ac.getThenStatements().size());
    }
    
    @Test
    @DisplayName("Should handle 'And' statements")
    void shouldHandleAndStatements() {
        String storyText = "{panel:title=Value Statement}\n" +
            "As a user, I want something, so that benefit.\n" +
            "{panel}\n" +
            "{panel:title=Acceptance Criteria}\n" +
            "Scenario: Multiple conditions\n" +
            "Given a precondition\n" +
            "And another precondition\n" +
            "When an action occurs\n" +
            "And another action occurs\n" +
            "Then an outcome happens\n" +
            "And another outcome happens\n" +
            "{panel}";
        
        JiraStory story = parser.parse(storyText);
        AcceptanceCriterion ac = story.getAcceptanceCriteria().get(0);
        
        assertEquals(2, ac.getGivenStatements().size());
        assertEquals(2, ac.getWhenStatements().size());
        assertEquals(2, ac.getThenStatements().size());
    }
}
