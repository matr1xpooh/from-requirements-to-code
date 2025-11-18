package an.story.gherkin_generator.command;

import an.story.domain_model.AcceptanceCriterion;
import an.story.domain_model.JiraStory;
import an.story.domain_model.ServiceTopology;
import an.story.parser.JiraStoryParser;

/**
 * Command to generate a complete Gherkin feature file from a Jira story
 */
public class GenerateFeatureFileFromStoryCommand implements Command<String> {
    private final JiraStory story;
    
    public GenerateFeatureFileFromStoryCommand(JiraStory story) {
        this.story = story;
    }
    
    @Override
    public String execute() {
        StringBuilder feature = new StringBuilder();
        
        // Feature header
        feature.append("Feature: ").append(new GenerateFeatureNameCommand(story).execute()).append("\n");
        feature.append("  ").append(story.getValueStatement().toString()).append("\n\n");
        
        // Background section if needed
        ServiceTopology topology = new JiraStoryParser().extractTopology(story);
        if (!topology.getServices().isEmpty()) {
            feature.append(new GenerateBackgroundCommand(topology).execute());
        }
        
        // Scenarios from acceptance criteria
        for (AcceptanceCriterion criterion : story.getAcceptanceCriteria()) {
            feature.append(new GenerateScenarioCommand(criterion).execute());
            feature.append("\n");
        }
        
        return feature.toString();
    }
}

