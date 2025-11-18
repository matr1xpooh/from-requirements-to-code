package an.story.gherkin_generator.command;

import an.story.domain_model.JiraStory;
import an.story.gherkin_generator.model.TestPackage;

/**
 * Command to generate a complete test package (feature file and step definitions) from a Jira story
 */
public class GenerateCompleteTestPackageCommand implements Command<TestPackage> {
    private final JiraStory story;
    // private final String basePackage;
    
    public GenerateCompleteTestPackageCommand(JiraStory story, String basePackage) {
        this.story = story;
    }
    
    @Override
    public TestPackage execute() {
        return new TestPackage(
            new GenerateFeatureFileFromStoryCommand(story).execute()
        );
    }
}

