package an.story.domain_model;


/**
 * Represents the "As a... I want... so that..." value statement
 */
public class ValueStatement {
    private String persona;
    private String goal;
    private String benefit;

    public ValueStatement(String persona, String goal, String benefit) {
        this.persona = persona;
        this.goal = goal;
        this.benefit = benefit;
    }

    public String getPersona() { return persona; }
    public String getGoal() { return goal; }
    public String getBenefit() { return benefit; }

    @Override
    public String toString() {
        return String.format("As a %s, I want %s, so that %s", persona, goal, benefit);
    }
}