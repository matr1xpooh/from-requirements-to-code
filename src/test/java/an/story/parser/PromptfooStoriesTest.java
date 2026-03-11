package an.story.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import an.story.domain_model.AcceptanceCriterion;
import an.story.domain_model.JiraStory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Promptfoo Stories Parsing")
public class PromptfooStoriesTest extends JiraStoryParserTest {

    private String story7861;
    private String story7845;
    private String story7823;
    private String story7757;
    private String story7727;

    @BeforeEach
    void loadPromptfooStories() throws IOException {
        story7861 = loadStory("/stories/promptfoo-7861.story");
        story7845 = loadStory("/stories/promptfoo-7845.story");
        story7823 = loadStory("/stories/promptfoo-7823.story");
        story7757 = loadStory("/stories/promptfoo-7757.story");
        story7727 = loadStory("/stories/promptfoo-7727.story");
    }

    private String loadStory(String path) throws IOException {
        InputStream stream = getClass().getResourceAsStream(path);
        assertNotNull(stream, "Story file not found: " + path);
        try (stream) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // ── #7861 – llm-rubric template variable resolution ──────────────────────

    @Test
    @DisplayName("#7861: Should parse value statement for llm-rubric story")
    void story7861_shouldParseValueStatement() {
        JiraStory story = parser.parse(story7861);
        assertNotNull(story.getValueStatement());
        assertEquals("promptfoo user", story.getValueStatement().getPersona());
        assertTrue(story.getValueStatement().getGoal().contains("template variables"));
    }

    @Test
    @DisplayName("#7861: Should parse 3 requirements for llm-rubric story")
    void story7861_shouldParse3Requirements() {
        JiraStory story = parser.parse(story7861);
        assertEquals(3, story.getRequirements().size());
        assertTrue(story.getRequirements().get(0).getText().contains("assertion evaluation pipeline"));
        assertTrue(story.getRequirements().get(1).getText().contains("per-test-case variables"));
        assertTrue(story.getRequirements().get(2).getText().contains("actual variable value"));
    }

    @Test
    @DisplayName("#7861: Should parse 2 acceptance criteria for llm-rubric story")
    void story7861_shouldParse2AcceptanceCriteria() {
        JiraStory story = parser.parse(story7861);
        assertEquals(2, story.getAcceptanceCriteria().size());

        AcceptanceCriterion first = story.getAcceptanceCriteria().get(0);
        assertEquals("llm-rubric in defaultTest resolves template variables correctly", first.getScenarioName());
        assertEquals(1, first.getGivenStatements().size());
        assertEquals(1, first.getWhenStatements().size());
        assertEquals(1, first.getThenStatements().size());

        AcceptanceCriterion second = story.getAcceptanceCriteria().get(1);
        assertEquals("inline llm-rubric continues to resolve variables", second.getScenarioName());
    }

    // ── #7845 – _conversation false-positive substring detection ─────────────

    @Test
    @DisplayName("#7845: Should parse value statement for _conversation story")
    void story7845_shouldParseValueStatement() {
        JiraStory story = parser.parse(story7845);
        assertNotNull(story.getValueStatement());
        assertEquals("promptfoo user", story.getValueStatement().getPersona());
        assertTrue(story.getValueStatement().getGoal().contains("_conversation"));
    }

    @Test
    @DisplayName("#7845: Should parse 3 requirements for _conversation story")
    void story7845_shouldParse3Requirements() {
        JiraStory story = parser.parse(story7845);
        assertEquals(3, story.getRequirements().size());
        assertTrue(story.getRequirements().get(0).getText().contains("substring check"));
        assertTrue(story.getRequirements().get(1).getText().contains("concurrency=1"));
        assertTrue(story.getRequirements().get(2).getText().contains("unit tests"));
    }

    @Test
    @DisplayName("#7845: Should parse 2 acceptance criteria for _conversation story")
    void story7845_shouldParse2AcceptanceCriteria() {
        JiraStory story = parser.parse(story7845);
        assertEquals(2, story.getAcceptanceCriteria().size());

        AcceptanceCriterion first = story.getAcceptanceCriteria().get(0);
        assertEquals("Prompt containing _conversation as a substring does not force concurrency=1", first.getScenarioName());
        assertEquals(1, first.getGivenStatements().size());
        assertEquals(1, first.getWhenStatements().size());
        assertEquals(1, first.getThenStatements().size());

        AcceptanceCriterion second = story.getAcceptanceCriteria().get(1);
        assertEquals("Prompt using {{ _conversation }} variable correctly forces concurrency=1", second.getScenarioName());
    }

    // ── #7823 – External file references for nested assertion properties ─────

    @Test
    @DisplayName("#7823: Should parse value statement for external file reference story")
    void story7823_shouldParseValueStatement() {
        JiraStory story = parser.parse(story7823);
        assertNotNull(story.getValueStatement());
        assertEquals("promptfoo test suite maintainer", story.getValueStatement().getPersona());
        assertTrue(story.getValueStatement().getGoal().contains("external files"));
    }

    @Test
    @DisplayName("#7823: Should parse 3 requirements for external file reference story")
    void story7823_shouldParse3Requirements() {
        JiraStory story = parser.parse(story7823);
        assertEquals(3, story.getRequirements().size());
        assertTrue(story.getRequirements().get(0).getText().contains("file://"));
        assertTrue(story.getRequirements().get(2).getText().contains("document"));
    }

    @Test
    @DisplayName("#7823: Should parse 3 acceptance criteria for external file reference story")
    void story7823_shouldParse3AcceptanceCriteria() {
        JiraStory story = parser.parse(story7823);
        assertEquals(3, story.getAcceptanceCriteria().size());
        assertEquals("External file reference resolves for nested \"value\" property",
                story.getAcceptanceCriteria().get(0).getScenarioName());
        assertEquals("External file reference resolves for nested \"provider\" property",
                story.getAcceptanceCriteria().get(1).getScenarioName());
        assertEquals("Unsupported property does not resolve file reference",
                story.getAcceptanceCriteria().get(2).getScenarioName());
    }

    // ── #7757 – Static export of evaluation results ──────────────────────────

    @Test
    @DisplayName("#7757: Should parse value statement for static export story")
    void story7757_shouldParseValueStatement() {
        JiraStory story = parser.parse(story7757);
        assertNotNull(story.getValueStatement());
        assertTrue(story.getValueStatement().getGoal().contains("static HTML site"));
    }

    @Test
    @DisplayName("#7757: Should parse 3 requirements for static export story")
    void story7757_shouldParse3Requirements() {
        JiraStory story = parser.parse(story7757);
        assertEquals(3, story.getRequirements().size());
        assertTrue(story.getRequirements().get(0).getText().contains("static"));
        assertTrue(story.getRequirements().get(1).getText().contains("server"));
        assertTrue(story.getRequirements().get(2).getText().contains("GitHub Pages"));
    }

    @Test
    @DisplayName("#7757: Should parse 3 acceptance criteria for static export story")
    void story7757_shouldParse3AcceptanceCriteria() {
        JiraStory story = parser.parse(story7757);
        assertEquals(3, story.getAcceptanceCriteria().size());
        assertEquals("Static export is generated after a CI evaluation run",
                story.getAcceptanceCriteria().get(0).getScenarioName());
        assertEquals("Static site displays evaluation results without a server",
                story.getAcceptanceCriteria().get(1).getScenarioName());
    }

    // ── #7727 – maxRetries=0 for 429 rate limits ─────────────────────────────

    @Test
    @DisplayName("#7727: Should parse value statement for maxRetries story")
    void story7727_shouldParseValueStatement() {
        JiraStory story = parser.parse(story7727);
        assertNotNull(story.getValueStatement());
        assertTrue(story.getValueStatement().getGoal().contains("maximum retry count"));
        assertTrue(story.getValueStatement().getBenefit().contains("fail fast"));
    }

    @Test
    @DisplayName("#7727: Should parse 3 requirements for maxRetries story")
    void story7727_shouldParse3Requirements() {
        JiraStory story = parser.parse(story7727);
        assertEquals(3, story.getRequirements().size());
        assertTrue(story.getRequirements().get(0).getText().contains("maxRetries"));
        assertTrue(story.getRequirements().get(1).getText().contains("429"));
        assertTrue(story.getRequirements().get(2).getText().contains("built-in providers"));
    }

    @Test
    @DisplayName("#7727: Should parse 3 acceptance criteria for maxRetries story")
    void story7727_shouldParse3AcceptanceCriteria() {
        JiraStory story = parser.parse(story7727);
        assertEquals(3, story.getAcceptanceCriteria().size());
        assertEquals("Provider with maxRetries=0 fails immediately on 429",
                story.getAcceptanceCriteria().get(0).getScenarioName());
        assertEquals("Provider with default maxRetries retries on 429",
                story.getAcceptanceCriteria().get(1).getScenarioName());
        assertEquals("maxRetries is respected across all built-in providers",
                story.getAcceptanceCriteria().get(2).getScenarioName());
    }
}
