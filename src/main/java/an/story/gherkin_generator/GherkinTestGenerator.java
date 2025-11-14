package an.story.gherkin_generator;

import java.util.*;
import java.util.stream.Collectors;

import an.story.domain_model.JiraStory;
import an.story.domain_model.ServiceTopology;
import an.story.gherkin_generator.model.TestPackage;
import an.story.domain_model.AcceptanceCriterion;
import an.story.parser.JiraStoryParser;

/**
 * Generates Gherkin feature files and step definition stubs from parsed Jira stories
 */
public class GherkinTestGenerator {
    
    /**
     * Generate a complete Gherkin feature file from a parsed story
     */
    public String generateFeatureFile(JiraStory story) {
        StringBuilder feature = new StringBuilder();
        
        // Feature header
        feature.append("Feature: ").append(generateFeatureName(story)).append("\n");
        feature.append("  ").append(story.getValueStatement().toString()).append("\n\n");
        
        // Background section if needed
        ServiceTopology topology = new JiraStoryParser().extractTopology(story);
        if (!topology.getServices().isEmpty()) {
            feature.append(generateBackground(topology));
        }
        
        // Scenarios from acceptance criteria
        for (AcceptanceCriterion criterion : story.getAcceptanceCriteria()) {
            feature.append(generateScenario(criterion));
            feature.append("\n");
        }
        
        return feature.toString();
    }
    
    private String generateFeatureName(JiraStory story) {
        String goal = story.getValueStatement().getGoal();
        return goal.substring(0, 1).toUpperCase() + goal.substring(1);
    }
    
    private String generateBackground(ServiceTopology topology) {
        StringBuilder bg = new StringBuilder();
        bg.append("  Background:\n");
        
        for (String service : topology.getServices()) {
            bg.append("    Given the \"").append(service).append("\" service is running\n");
        }
        
        if (!topology.getEvents().isEmpty()) {
            bg.append("    And event consumers are ready to receive events\n");
        }
        
        if (!topology.getSchemas().isEmpty()) {
            bg.append("    And the Avro schema registry is accessible\n");
        }
        
        bg.append("\n");
        return bg.toString();
    }
    
    private String generateScenario(AcceptanceCriterion criterion) {
        StringBuilder scenario = new StringBuilder();
        
        scenario.append("  Scenario: ").append(criterion.getScenarioName()).append("\n");
        
        // Given statements
        for (int i = 0; i < criterion.getGivenStatements().size(); i++) {
            String keyword = i == 0 ? "Given" : "And";
            scenario.append("    ").append(keyword).append(" ")
                    .append(criterion.getGivenStatements().get(i)).append("\n");
        }
        
        // When statements
        for (int i = 0; i < criterion.getWhenStatements().size(); i++) {
            String keyword = i == 0 ? "When" : "And";
            scenario.append("    ").append(keyword).append(" ")
                    .append(criterion.getWhenStatements().get(i)).append("\n");
        }
        
        // Then statements
        for (int i = 0; i < criterion.getThenStatements().size(); i++) {
            String keyword = i == 0 ? "Then" : "And";
            scenario.append("    ").append(keyword).append(" ")
                    .append(criterion.getThenStatements().get(i)).append("\n");
        }
        
        return scenario.toString();
    }
    
    /**
     * Generate Java step definition class that uses the static test infrastructure
     */
    public String generateStepDefinitions(JiraStory story, String packageName) {
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
        String className = generateStepDefinitionClassName(story);
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
        Set<String> uniqueSteps = collectUniqueSteps(story);
        
        // Generate step methods
        for (String step : uniqueSteps) {
            steps.append(generateStepMethod(step));
        }
        
        steps.append("}\n");
        
        return steps.toString();
    }
    
    private String generateStepDefinitionClassName(JiraStory story) {
        String goal = story.getValueStatement().getGoal();
        String[] words = goal.split("\\s+");
        StringBuilder className = new StringBuilder();
        
        for (int i = 0; i < Math.min(words.length, 5); i++) {
            String word = words[i].replaceAll("[^a-zA-Z]", "");
            if (!word.isEmpty()) {
                className.append(word.substring(0, 1).toUpperCase())
                         .append(word.substring(1).toLowerCase());
            }
        }
        
        className.append("Steps");
        return className.toString();
    }
    
    private Set<String> collectUniqueSteps(JiraStory story) {
        Set<String> steps = new LinkedHashSet<>();
        
        for (AcceptanceCriterion criterion : story.getAcceptanceCriteria()) {
            criterion.getGivenStatements().forEach(steps::add);
            criterion.getWhenStatements().forEach(steps::add);
            criterion.getThenStatements().forEach(steps::add);
        }
        
        return steps;
    }
    
    private String generateStepMethod(String stepText) {
        StringBuilder method = new StringBuilder();
        
        String annotation = determineAnnotation(stepText);
        String cucumberExpression = stepText;
        String methodName = generateMethodName(stepText);
        
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
    
    private String determineAnnotation(String stepText) {
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
    
    private String generateMethodName(String stepText) {
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
    
    /**
     * Generate complete test package
     */
    public TestPackage generateCompleteTestPackage(JiraStory story, String basePackage) {
        return new TestPackage(
            generateFeatureFile(story),
            generateStepDefinitions(story, basePackage + ".steps")
        );
    }
}

