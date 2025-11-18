package an.story.gherkin_generator;

import an.story.domain_model.JiraStory;
import an.story.gherkin_generator.command.GenerateCompleteTestPackageCommand;
import an.story.gherkin_generator.command.GenerateFeatureFileFromStoryCommand;
import an.story.gherkin_generator.command.GenerateStepDefinitionsCommand;
import an.story.gherkin_generator.model.TestPackage;

/**
 * Generates Gherkin feature files and step definition stubs from parsed Jira stories
 */
public class GherkinTestGenerator {
    
    public String generateFeatureFileFromStory(JiraStory story) {
        return new GenerateFeatureFileFromStoryCommand(story).execute();
    }
    
    /**
     * Generate Java step definition class that uses the static test infrastructure
     */
    public String generateStepDefinitions(JiraStory story, String packageName) {
        return new GenerateStepDefinitionsCommand(story, packageName).execute();
    }
    
    /**
     * Generate complete test package
     */
    public TestPackage generateCompleteTestPackage(JiraStory story, String basePackage) {
        return new GenerateCompleteTestPackageCommand(story, basePackage).execute();
    }
}

