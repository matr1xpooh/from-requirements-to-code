package an.story.domain_model;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a parsed Jira story with all its components
 */
public class JiraStory {
    private ValueStatement valueStatement;
    private List<Requirement> requirements;
    private List<AcceptanceCriterion> acceptanceCriteria;

    public JiraStory(ValueStatement valueStatement, List<Requirement> requirements, 
                     List<AcceptanceCriterion> acceptanceCriteria) {
        this.valueStatement = valueStatement;
        this.requirements = requirements != null ? requirements : new ArrayList<>();
        this.acceptanceCriteria = acceptanceCriteria != null ? acceptanceCriteria : new ArrayList<>();
    }

    public ValueStatement getValueStatement() { return valueStatement; }
    public List<Requirement> getRequirements() { return requirements; }
    public List<AcceptanceCriterion> getAcceptanceCriteria() { return acceptanceCriteria; }

    @Override
    public String toString() {
        return "JiraStory{" +
                "valueStatement=" + valueStatement +
                ", requirements=" + requirements.size() +
                ", acceptanceCriteria=" + acceptanceCriteria.size() +
                '}';
    }
}
