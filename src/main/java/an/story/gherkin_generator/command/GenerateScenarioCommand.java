 package an.story.gherkin_generator.command;

import an.story.domain_model.AcceptanceCriterion;

import java.util.List;

/**
 * Command to generate a Scenario section from an AcceptanceCriterion
 */
public class GenerateScenarioCommand implements Command<String> {
    private final AcceptanceCriterion criterion;
    
    public GenerateScenarioCommand(AcceptanceCriterion criterion) {
        this.criterion = criterion;
    }
    
    @Override
    public String execute() {
        StringBuilder scenario = new StringBuilder();
        
        scenario.append("  Scenario: ").append(criterion.getScenarioName()).append("\n");
        
        appendStatements(scenario, criterion.getGivenStatements(), "Given");
        appendStatements(scenario, criterion.getWhenStatements(), "When");
        appendStatements(scenario, criterion.getThenStatements(), "Then");
        
        return scenario.toString();
    }
    
    private void appendStatements(StringBuilder scenario, List<String> statements, String primaryKeyword) {
        for (int i = 0; i < statements.size(); i++) {
            String keyword = i == 0 ? primaryKeyword : "And";
            scenario.append("    ").append(keyword).append(" ")
                    .append(statements.get(i)).append("\n");
        }
    }
}

