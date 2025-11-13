package an.story.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

@DisplayName("Jira Story Parser Tests")
public class JiraStoryParserTest{

    protected static String completeStoryText;

    protected JiraStoryParser parser;

    @BeforeAll
    static void loadCompleteStory() throws IOException {
        Path storyPath = Path.of("sample-jira.story");
        if (!Files.exists(storyPath)) {
            throw new IllegalArgumentException("Story file not found: " + storyPath.toAbsolutePath());
        }
        completeStoryText = Files.readString(storyPath, StandardCharsets.UTF_8);
    }
    
    @BeforeEach
    void setUp() {
        parser = new JiraStoryParser();
    }

}