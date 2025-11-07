package an.story.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Error Handling")
class ErrorHandlingTests extends JiraStoryParserTest{
    
    @Test
    @DisplayName("Should throw exception for null input")
    void shouldThrowExceptionForNullInput() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
    }
    
    @Test
    @DisplayName("Should throw exception for empty input")
    void shouldThrowExceptionForEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(""));
    }
    
    @Test
    @DisplayName("Should throw exception for malformed value statement")
    void shouldThrowExceptionForMalformedValueStatement() {
        String storyText = "{panel:title=Value Statement}\n" +
            "This is not a valid value statement format\n" +
            "{panel}";
        
        assertThrows(IllegalArgumentException.class, () -> parser.parse(storyText));
    }
}