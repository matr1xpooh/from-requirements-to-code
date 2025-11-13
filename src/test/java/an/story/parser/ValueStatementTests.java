package an.story.parser;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import an.story.domain_model.JiraStory;
import an.story.domain_model.ValueStatement;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Value Statement Parsing")
public class ValueStatementTests extends JiraStoryParserTest {
    
    @Test
    @DisplayName("Should parse valid value statement")
    void shouldParseValueStatement() {
        String storyText = "{panel:title=Value Statement | titleBGColor=#b9d9ed}\n" +
            "As a Chase Bank product owner, I want to ensure that users over age of 120 are not allowed to apply for a credit card, so that we\n" +
            "maintain data integrity and comply with realistic age expectations.\n" +
            "{panel}";
        
        JiraStory story = parser.parse(storyText);
        ValueStatement vs = story.getValueStatement();
        
        assertNotNull(vs);
        assertEquals("Chase Bank product owner", vs.getPersona());
        assertTrue(vs.getGoal().contains("ensure that users over age of 120"));
        assertTrue(vs.getBenefit().contains("maintain data integrity"));
    }
    
    @Test
    @DisplayName("Should handle value statement with 'to' after 'I want'")
    void shouldHandleValueStatementWithTo() {
        String storyText = "{panel:title=Value Statement | titleBGColor=#b9d9ed}\n" +
            "As a developer, I want to write clean code, so that it is maintainable.\n" +
            "{panel}";
        
        JiraStory story = parser.parse(storyText);
        ValueStatement vs = story.getValueStatement();
        
        assertEquals("developer", vs.getPersona());
        assertEquals("write clean code", vs.getGoal());
        assertEquals("it is maintainable.", vs.getBenefit());
    }
    
    @Test
    @DisplayName("Should throw exception when value statement is missing")
    void shouldThrowExceptionWhenValueStatementMissing() {
        String storyText = "{panel:title=Requirements | titleBGColor=#b9d9ed}\n" +
            "1. some requirement\n" +
            "{panel}";
        
        assertThrows(IllegalArgumentException.class, () -> parser.parse(storyText));
    }
}