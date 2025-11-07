package an.story.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

@DisplayName("Jira Story Parser Tests")
public class JiraStoryParserTest{
    
    protected JiraStoryParser parser;
    
    @BeforeEach
    void setUp() {
        parser = new JiraStoryParser();
    }

}