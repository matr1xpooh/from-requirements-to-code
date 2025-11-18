package an.story.gherkin_generator.command;

/**
 * Command to generate a step method from step text
 */
public class GenerateStepMethodCommand implements Command<String> {
    private final String stepText;
    
    public GenerateStepMethodCommand(String stepText) {
        this.stepText = stepText;
    }
    
    @Override
    public String execute() {
        StringBuilder method = new StringBuilder();
        
        String annotation = new DetermineAnnotationCommand(stepText).execute();
        String cucumberExpression = stepText;
        String methodName = new GenerateMethodNameCommand(stepText).execute();
        
        method.append("    @").append(annotation).append("(\"").append(cucumberExpression).append("\")\n");
        method.append("    public void ").append(methodName).append("() {\n");
        method.append("        // TODO: Implement this step\n");
        method.append("        // Available: harness.getService(\"serviceName\")\n");
        method.append("        //           harness.waitForEvent(\"eventType\", timeoutSeconds)\n");
        method.append("        //           harness.verifyEventNotPublished(\"eventType\")\n");
        method.append("        //           context.set(\"key\", value)\n");
        method.append("        throw new io.cucumber.java.PendingException();\n");
        method.append("    }\n\n");
        
        return method.toString();
    }
}

