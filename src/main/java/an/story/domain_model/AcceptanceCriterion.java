package an.story.domain_model;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a Gherkin-style acceptance criterion (Scenario with Given/When/Then)
 */
public class AcceptanceCriterion {
    private String scenarioName;
    private List<String> givenStatements;
    private List<String> whenStatements;
    private List<String> thenStatements;

    public AcceptanceCriterion(String scenarioName) {
        this.scenarioName = scenarioName;
        this.givenStatements = new ArrayList<>();
        this.whenStatements = new ArrayList<>();
        this.thenStatements = new ArrayList<>();
    }

    public void addGiven(String statement) { givenStatements.add(statement); }
    public void addWhen(String statement) { whenStatements.add(statement); }
    public void addThen(String statement) { thenStatements.add(statement); }

    public String getScenarioName() { return scenarioName; }
    public List<String> getGivenStatements() { return givenStatements; }
    public List<String> getWhenStatements() { return whenStatements; }
    public List<String> getThenStatements() { return thenStatements; }

    @Override
    public String toString() {
        return String.format("Scenario: %s [Given:%d, When:%d, Then:%d]",
                           scenarioName, givenStatements.size(), 
                           whenStatements.size(), thenStatements.size());
    }
}