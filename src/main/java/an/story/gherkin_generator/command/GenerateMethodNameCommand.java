package an.story.gherkin_generator.command;

/**
 * Command to generate a method name from step text
 */
public class GenerateMethodNameCommand implements Command<String> {
    private final String stepText;
    
    public GenerateMethodNameCommand(String stepText) {
        this.stepText = stepText;
    }
    
    @Override
    public String execute() {
        String cleaned = stepText.replaceAll("\"[^\"]*\"", "param")
                                 .replaceAll("[^a-zA-Z0-9\\s]", "")
                                 .trim();
        
        String[] words = cleaned.split("\\s+");
        StringBuilder methodName = new StringBuilder();
        
        for (int i = 0; i < Math.min(words.length, 6); i++) {
            if (words[i].isEmpty()) continue;
            
            if (methodName.length() == 0) {
                methodName.append(words[i].toLowerCase());
            } else {
                methodName.append(words[i].substring(0, 1).toUpperCase())
                         .append(words[i].substring(1).toLowerCase());
            }
        }
        
        return methodName.toString();
    }
}

