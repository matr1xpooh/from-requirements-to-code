package an.story.gherkin_generator.command;

/**
 * Command to determine the Cucumber annotation (Given/When/Then) from step text
 */
public class DetermineAnnotationCommand implements Command<String> {
    private final String stepText;
    
    public DetermineAnnotationCommand(String stepText) {
        this.stepText = stepText;
    }
    
    @Override
    public String execute() {
        String lower = stepText.toLowerCase();
        if (lower.contains("is") || lower.contains("are") || lower.contains("has") || 
            lower.contains("have") || lower.contains("exists") || lower.contains("running")) {
            return "Given";
        } else if (lower.contains("processed") || lower.contains("triggered") || 
                   lower.contains("called") || lower.contains("submitted")) {
            return "When";
        } else {
            return "Then";
        }
    }
}

