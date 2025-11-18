package an.story.gherkin_generator.command;

import an.story.domain_model.JiraStory;

/**
 * Command to generate a step definition class name from a Jira story
 */
public class GenerateStepDefinitionClassNameCommand implements Command<String> {
    private final JiraStory story;
    
    public GenerateStepDefinitionClassNameCommand(JiraStory story) {
        this.story = story;
    }
    
    @Override
    public String execute() {
        String goal = story.getValueStatement().getGoal();
        String[] words = goal.split("\\s+");
        StringBuilder className = new StringBuilder();
        
        for (int i = 0; i < Math.min(words.length, 5); i++) {
            String word = words[i].replaceAll("[^a-zA-Z]", "");
            if (!word.isEmpty()) {
                className.append(word.substring(0, 1).toUpperCase())
                         .append(word.substring(1).toLowerCase());
            }
        }
        
        className.append("Steps");
        return className.toString();
    }
}

