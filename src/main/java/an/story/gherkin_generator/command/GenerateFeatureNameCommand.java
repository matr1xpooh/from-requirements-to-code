package an.story.gherkin_generator.command;

import an.story.domain_model.JiraStory;

/**
 * Command to generate a feature name from a Jira story
 */
public class GenerateFeatureNameCommand implements Command<String> {
    private final JiraStory story;
    
    public GenerateFeatureNameCommand(JiraStory story) {
        this.story = story;
    }
    
    @Override
    public String execute() {
        String goal = story.getValueStatement().getGoal();
        return goal.substring(0, 1).toUpperCase() + goal.substring(1);
    }
}

