package an.story.gherkin_generator.command;

import an.story.domain_model.AcceptanceCriterion;
import an.story.domain_model.JiraStory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Command to collect unique step definitions from a Jira story
 */
public class CollectUniqueStepsCommand implements Command<Set<String>> {
    private final JiraStory story;
    
    public CollectUniqueStepsCommand(JiraStory story) {
        this.story = story;
    }
    
    @Override
    public Set<String> execute() {
        Set<String> steps = new LinkedHashSet<>();
        
        for (AcceptanceCriterion criterion : story.getAcceptanceCriteria()) {
            criterion.getGivenStatements().forEach(steps::add);
            criterion.getWhenStatements().forEach(steps::add);
            criterion.getThenStatements().forEach(steps::add);
        }
        
        return steps;
    }
}

