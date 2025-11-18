package an.story.gherkin_generator.command;

import an.story.domain_model.JiraStory;
import an.story.domain_model.ServiceTopology;
import an.story.parser.JiraStoryParser;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command to generate Java step definition class from a Jira story
 */
public class GenerateStepDefinitionsCommand implements Command<String> {
    private final JiraStory story;
    private final String packageName;
    
    public GenerateStepDefinitionsCommand(JiraStory story, String packageName) {
        this.story = story;
        this.packageName = packageName;
    }
    
    @Override
    public String execute() {
        StringBuilder steps = new StringBuilder();
        
        // Package and imports
        steps.append("package ").append(packageName).append(";\n\n");
        steps.append("import java.util.Arrays;\n");
        steps.append("import java.util.List;\n");
        steps.append("import io.cucumber.java.en.*;\n");
        steps.append("import io.cucumber.java.Before;\n");
        steps.append("import io.cucumber.java.After;\n");
        steps.append("import static org.junit.jupiter.api.Assertions.*;\n");
        steps.append("import an.story.gherkin_generator.TestContext;\n");
        steps.append("import an.story.gherkin_generator.MultiServiceTestHarness;\n\n");
        
        // Class
        String className = new GenerateStepDefinitionClassNameCommand(story).execute();
        steps.append("public class ").append(className).append(" {\n\n");
        
        // Test context and harness
        steps.append("    private TestContext context;\n");
        steps.append("    private MultiServiceTestHarness harness;\n\n");
        
        // Constructor with dependency injection
        steps.append("    public ").append(className).append("(TestContext context) {\n");
        steps.append("        this.context = context;\n");
        steps.append("    }\n\n");
        
        // Setup hook
        ServiceTopology topology = new JiraStoryParser().extractTopology(story);
        steps.append("    @Before\n");
        steps.append("    public void setUp() throws Exception {\n");
        if (topology.getServices().isEmpty()) {
            steps.append("        List<String> services = Arrays.asList();\n");
        } else {
            steps.append("        List<String> services = Arrays.asList(");
            steps.append(topology.getServices().stream()
                    .map(s -> "\"" + s + "\"")
                    .collect(Collectors.joining(", ")));
            steps.append(");\n");
        }
        if (topology.getEvents().isEmpty()) {
            steps.append("        List<String> events = Arrays.asList();\n");
        } else {
            steps.append("        List<String> events = Arrays.asList(");
            steps.append(topology.getEvents().stream()
                    .map(e -> "\"" + e + "\"")
                    .collect(Collectors.joining(", ")));
            steps.append(");\n");
        }
        steps.append("        harness = new MultiServiceTestHarness(services, events);\n");
        steps.append("        harness.setup();\n");
        steps.append("        context.setHarness(harness);\n");
        steps.append("    }\n\n");
        
        // Teardown hook
        steps.append("    @After\n");
        steps.append("    public void tearDown() throws Exception {\n");
        steps.append("        if (harness != null) {\n");
        steps.append("            harness.teardown();\n");
        steps.append("        }\n");
        steps.append("    }\n\n");
        
        // Collect unique step definitions
        Set<String> uniqueSteps = new CollectUniqueStepsCommand(story).execute();
        
        // Generate step methods
        for (String step : uniqueSteps) {
            steps.append(new GenerateStepMethodCommand(step).execute());
        }
        
        steps.append("}\n");
        
        return steps.toString();
    }
}

