package an.story.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;

@DisplayName("Jira Story Parser Tests")
public class JiraStoryParserTest{

    protected static String completeStoryText;

    protected JiraStoryParser parser;

    @BeforeAll
    static void loadCompleteStory() throws IOException {
        InputStream resourceStream = JiraStoryParserTest.class.getResourceAsStream("/sample-jira.story");
        if (resourceStream == null) {
            throw new IllegalArgumentException("Story file not found on the classpath: /sample-jira.story");
        }

        try (resourceStream) {
            completeStoryText = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    @BeforeEach
    void setUp() {
        parser = new JiraStoryParser();
    }

}