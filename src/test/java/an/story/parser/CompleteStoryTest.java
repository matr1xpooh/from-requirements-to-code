package an.story.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import an.story.domain_model.JiraStory;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Complete Story Parsing")
public class CompleteStoryTest extends JiraStoryParserTest {

    @Test
    @DisplayName("Should parse complete story with all sections")
    void shouldParseCompleteStory() {
        assertNotNull(JiraStoryParserTest.completeStoryText, "Complete story text should be loaded");
        JiraStory story = parser.parse(JiraStoryParserTest.completeStoryText);
        
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

