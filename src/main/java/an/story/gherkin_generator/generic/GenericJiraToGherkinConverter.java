package an.story.gherkin_generator.generic;

import an.story.domain_model.AcceptanceCriterion;
import an.story.domain_model.JiraStory;
import an.story.parser.JiraStoryParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic converter that transforms Jira stories with business-oriented acceptance
 * criteria into API-aware Gherkin scenarios.
 *
 * The converter uses pluggable DomainMapping implementations to understand how
 * business concepts in a specific domain map to technical API operations.
 *
 * Usage:
 * <pre>
 * DomainMapping mapping = new CreditCardTransactionMapping();
 * GenericJiraToGherkinConverter converter = new GenericJiraToGherkinConverter(mapping);
 * String featureFile = converter.convertFromFile(storyPath);
 * </pre>
 */
public class GenericJiraToGherkinConverter {

    private final JiraStoryParser parser;
    private final DomainMapping domainMapping;
    private final ConversionOptions options;

    public GenericJiraToGherkinConverter(DomainMapping domainMapping) {
        this(domainMapping, ConversionOptions.defaults());
    }

    public GenericJiraToGherkinConverter(DomainMapping domainMapping, ConversionOptions options) {
        this.parser = new JiraStoryParser();
        this.domainMapping = domainMapping;
        this.options = options;
    }

    /**
     * Convert a Jira story file to an API-aware Gherkin feature file.
     */
    public ConversionResult convertFromFile(Path storyPath) throws IOException {
        String storyContent = Files.readString(storyPath, StandardCharsets.UTF_8);
        return convert(storyContent);
    }

    /**
     * Convert a Jira story from classpath resource.
     */
    public ConversionResult convertFromResource(String resourcePath) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            String storyContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return convert(storyContent);
        }
    }

    /**
     * Convert a Jira story string to an API-aware Gherkin feature file.
     */
    public ConversionResult convert(String storyContent) {
        JiraStory story = parser.parse(storyContent);
        return generateConversionResult(story);
    }

    private ConversionResult generateConversionResult(JiraStory story) {
        StringBuilder feature = new StringBuilder();
        List<String> warnings = new ArrayList<>();
        Set<String> uniqueSteps = new LinkedHashSet<>();

        // Header comments
        if (options.includeComments) {
            feature.append("# Auto-generated from Jira acceptance criteria\n");
            feature.append("# Domain: ").append(domainMapping.getDomainName()).append("\n");
            feature.append("# Generated: ").append(java.time.LocalDateTime.now()).append("\n");
            feature.append("#\n");
            feature.append("# This feature file contains API-aware scenarios that test:\n");
            for (ApiEndpoint endpoint : domainMapping.getApiEndpoints()) {
                feature.append("#   - ").append(endpoint).append("\n");
            }
            for (KafkaTopic topic : domainMapping.getKafkaTopics()) {
                feature.append("#   - Kafka: ").append(topic).append("\n");
            }
            feature.append("\n");
        }

        // Feature declaration
        feature.append("Feature: ").append(generateFeatureName(story)).append("\n");
        if (story.getValueStatement() != null) {
            feature.append("  ").append(story.getValueStatement().toString()).append("\n");
        }
        feature.append("\n");

        // Background section
        List<String> backgroundSteps = domainMapping.getBackgroundSteps();
        if (!backgroundSteps.isEmpty()) {
            feature.append("  Background: Test infrastructure setup\n");
            for (String step : backgroundSteps) {
                feature.append("    ").append(step).append("\n");
                uniqueSteps.add(step);
            }
            feature.append("\n");
        }

        // Convert each acceptance criterion to a scenario
        for (AcceptanceCriterion criterion : story.getAcceptanceCriteria()) {
            TransformedScenario transformed = transformScenario(criterion, warnings);
            feature.append(formatScenario(transformed));
            uniqueSteps.addAll(transformed.allSteps);
            feature.append("\n");
        }

        // Generate step definitions
        String stepDefinitions = generateStepDefinitions(uniqueSteps, story);

        return new ConversionResult(
                feature.toString(),
                stepDefinitions,
                warnings,
                story
        );
    }

    private String generateFeatureName(JiraStory story) {
        if (story.getValueStatement() == null) {
            return domainMapping.getDomainName() + " Validation";
        }

        String goal = story.getValueStatement().getGoal();
        if (goal == null || goal.isEmpty()) {
            return domainMapping.getDomainName() + " Validation";
        }

        // Capitalize and clean up
        goal = goal.trim();
        goal = goal.substring(0, 1).toUpperCase() + goal.substring(1);

        // Remove common prefixes
        String[] prefixesToRemove = {"ensure that ", "verify that ", "validate that "};
        for (String prefix : prefixesToRemove) {
            if (goal.toLowerCase().startsWith(prefix)) {
                goal = goal.substring(prefix.length());
                goal = goal.substring(0, 1).toUpperCase() + goal.substring(1);
                break;
            }
        }

        return goal;
    }

    private TransformedScenario transformScenario(AcceptanceCriterion criterion,
                                                   List<String> warnings) {
        TransformedScenario scenario = new TransformedScenario();
        scenario.name = criterion.getScenarioName();

        // Transform Given statements
        for (String given : criterion.getGivenStatements()) {
            String transformed = domainMapping.transformStep(StepPattern.StepType.GIVEN, given);
            if (transformed.contains("TODO")) {
                warnings.add("Unmapped Given step: " + given);
            }
            scenario.givenSteps.add(transformed);
            scenario.allSteps.add(transformed);
        }

        // Transform When statements
        for (String when : criterion.getWhenStatements()) {
            String transformed = domainMapping.transformStep(StepPattern.StepType.WHEN, when);
            if (transformed.contains("TODO")) {
                warnings.add("Unmapped When step: " + when);
            }
            scenario.whenSteps.add(transformed);
            scenario.allSteps.add(transformed);
        }

        // Transform Then statements
        for (String then : criterion.getThenStatements()) {
            String transformed = domainMapping.transformStep(StepPattern.StepType.THEN, then);
            if (transformed.contains("TODO")) {
                warnings.add("Unmapped Then step: " + then);
            }
            scenario.thenSteps.add(transformed);
            scenario.allSteps.add(transformed);
        }

        return scenario;
    }

    private String formatScenario(TransformedScenario scenario) {
        StringBuilder sb = new StringBuilder();

        sb.append("  Scenario: ").append(scenario.name).append("\n");

        // Given steps
        for (int i = 0; i < scenario.givenSteps.size(); i++) {
            String keyword = (i == 0) ? "Given" : "And";
            sb.append("    ").append(keyword).append(" ")
              .append(scenario.givenSteps.get(i)).append("\n");
        }

        // When steps
        for (int i = 0; i < scenario.whenSteps.size(); i++) {
            String keyword = (i == 0) ? "When" : "And";
            sb.append("    ").append(keyword).append(" ")
              .append(scenario.whenSteps.get(i)).append("\n");
        }

        // Then steps
        for (int i = 0; i < scenario.thenSteps.size(); i++) {
            String keyword = (i == 0) ? "Then" : "And";
            sb.append("    ").append(keyword).append(" ")
              .append(scenario.thenSteps.get(i)).append("\n");
        }

        return sb.toString();
    }

    private String generateStepDefinitions(Set<String> uniqueSteps, JiraStory story) {
        StringBuilder code = new StringBuilder();
        String className = generateClassName(story);

        // Package and imports
        code.append("package ").append(options.stepDefinitionPackage).append(";\n\n");

        for (String imp : domainMapping.getStepDefinitionImports()) {
            code.append("import ").append(imp).append(";\n");
        }

        // Standard imports
        code.append("import io.cucumber.java.Before;\n");
        code.append("import io.cucumber.java.After;\n");
        code.append("import io.cucumber.java.en.Given;\n");
        code.append("import io.cucumber.java.en.When;\n");
        code.append("import io.cucumber.java.en.Then;\n");
        code.append("import static org.junit.jupiter.api.Assertions.*;\n\n");

        // Class declaration
        code.append("/**\n");
        code.append(" * Step definitions for ").append(domainMapping.getDomainName()).append(".\n");
        code.append(" * Auto-generated from Jira acceptance criteria.\n");
        code.append(" */\n");
        code.append("public class ").append(className).append(" {\n\n");

        // Instance variables
        for (String var : domainMapping.getStepDefinitionInstanceVariables()) {
            code.append("    ").append(var).append("\n");
        }
        code.append("\n");

        // Before hook
        code.append("    @Before\n");
        code.append("    public void setUp() {\n");
        code.append(indentCode(domainMapping.getBeforeHookCode(), 8));
        code.append("    }\n\n");

        // After hook
        code.append("    @After\n");
        code.append("    public void tearDown() {\n");
        code.append(indentCode(domainMapping.getAfterHookCode(), 8));
        code.append("    }\n\n");

        // Generate step methods
        Set<String> generatedPatterns = new HashSet<>();
        for (String step : uniqueSteps) {
            String methodCode = generateStepMethod(step, generatedPatterns);
            if (methodCode != null) {
                code.append(methodCode);
            }
        }

        code.append("}\n");

        return code.toString();
    }

    private String generateStepMethod(String step, Set<String> generatedPatterns) {
        // Extract the Cucumber pattern from the step
        String pattern = extractCucumberPattern(step);

        // Skip if we've already generated this pattern
        if (generatedPatterns.contains(pattern)) {
            return null;
        }
        generatedPatterns.add(pattern);

        // Determine the annotation type
        String annotation = determineAnnotation(step);

        // Generate method name
        String methodName = generateMethodName(step);

        // Generate method parameters
        List<String> params = extractParameters(pattern);

        StringBuilder method = new StringBuilder();
        method.append("    @").append(annotation).append("(\"").append(escapePattern(pattern)).append("\")\n");
        method.append("    public void ").append(methodName).append("(");
        method.append(formatParameters(params));
        method.append(") {\n");
        method.append("        // TODO: Implement this step\n");
        method.append("        throw new io.cucumber.java.PendingException();\n");
        method.append("    }\n\n");

        return method.toString();
    }

    private String extractCucumberPattern(String step) {
        // Convert a concrete step to a parameterized pattern
        String pattern = step;

        // Replace quoted strings with {string}
        pattern = pattern.replaceAll("\"[^\"]+\"", "{string}");

        // Replace numbers with {int} or {double}
        pattern = pattern.replaceAll("\\b\\d+\\.\\d+\\b", "{double}");
        pattern = pattern.replaceAll("\\b\\d+\\b", "{int}");

        return pattern;
    }

    private String determineAnnotation(String step) {
        // This is a simplification - in practice, we'd track which type of step this is
        String lower = step.toLowerCase();
        if (lower.startsWith("i have") || lower.startsWith("the ") && lower.contains("is ") ||
            lower.contains("set") || lower.contains("running") || lower.contains("listening")) {
            return "Given";
        }
        if (lower.startsWith("i post") || lower.startsWith("i get") ||
            lower.startsWith("i put") || lower.startsWith("i delete") ||
            lower.startsWith("i consume") || lower.startsWith("i send")) {
            return "When";
        }
        return "Then";
    }

    private String generateMethodName(String step) {
        // Convert step text to a valid Java method name
        String name = step
                .replaceAll("\"[^\"]+\"", "value")
                .replaceAll("\\d+\\.?\\d*", "number")
                .replaceAll("[^a-zA-Z\\s]", "")
                .trim();

        String[] words = name.split("\\s+");
        StringBuilder methodName = new StringBuilder();
        for (int i = 0; i < Math.min(words.length, 6); i++) {
            String word = words[i].toLowerCase();
            if (i == 0) {
                methodName.append(word);
            } else {
                methodName.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    methodName.append(word.substring(1));
                }
            }
        }

        return methodName.toString();
    }

    private List<String> extractParameters(String pattern) {
        List<String> params = new ArrayList<>();
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\{(string|int|double)}");
        java.util.regex.Matcher m = p.matcher(pattern);

        int counter = 1;
        while (m.find()) {
            String type = m.group(1);
            switch (type) {
                case "string":
                    params.add("String arg" + counter);
                    break;
                case "int":
                    params.add("int arg" + counter);
                    break;
                case "double":
                    params.add("double arg" + counter);
                    break;
            }
            counter++;
        }

        return params;
    }

    private String formatParameters(List<String> params) {
        return String.join(", ", params);
    }

    private String escapePattern(String pattern) {
        return pattern.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String indentCode(String code, int spaces) {
        String indent = " ".repeat(spaces);
        return Arrays.stream(code.split("\n"))
                .map(line -> indent + line)
                .collect(Collectors.joining("\n")) + "\n";
    }

    private String generateClassName(JiraStory story) {
        if (story.getValueStatement() == null) {
            return domainMapping.getDomainName().replaceAll("[^a-zA-Z]", "") + "StepDefinitions";
        }

        String goal = story.getValueStatement().getGoal();
        if (goal == null || goal.isEmpty()) {
            return domainMapping.getDomainName().replaceAll("[^a-zA-Z]", "") + "StepDefinitions";
        }

        String[] words = goal.split("\\s+");
        StringBuilder className = new StringBuilder();
        int wordCount = 0;
        for (String word : words) {
            if (wordCount >= 4) break;
            word = word.replaceAll("[^a-zA-Z]", "");
            if (!word.isEmpty()) {
                className.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    className.append(word.substring(1).toLowerCase());
                }
                wordCount++;
            }
        }
        className.append("StepDefinitions");
        return className.toString();
    }

    /**
     * Helper class to hold transformed scenario data.
     */
    private static class TransformedScenario {
        String name;
        List<String> givenSteps = new ArrayList<>();
        List<String> whenSteps = new ArrayList<>();
        List<String> thenSteps = new ArrayList<>();
        Set<String> allSteps = new LinkedHashSet<>();
    }

    /**
     * Configuration options for the conversion process.
     */
    public static class ConversionOptions {
        public boolean includeComments = true;
        public String stepDefinitionPackage = "steps";
        public boolean generatePendingExceptions = true;

        public static ConversionOptions defaults() {
            return new ConversionOptions();
        }

        public ConversionOptions withPackage(String packageName) {
            this.stepDefinitionPackage = packageName;
            return this;
        }

        public ConversionOptions withComments(boolean include) {
            this.includeComments = include;
            return this;
        }
    }

    /**
     * Result of a conversion operation.
     */
    public static class ConversionResult {
        private final String featureFile;
        private final String stepDefinitions;
        private final List<String> warnings;
        private final JiraStory originalStory;

        public ConversionResult(String featureFile, String stepDefinitions,
                               List<String> warnings, JiraStory originalStory) {
            this.featureFile = featureFile;
            this.stepDefinitions = stepDefinitions;
            this.warnings = warnings;
            this.originalStory = originalStory;
        }

        public String getFeatureFile() {
            return featureFile;
        }

        public String getStepDefinitions() {
            return stepDefinitions;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public JiraStory getOriginalStory() {
            return originalStory;
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public void writeToFiles(Path featurePath, Path stepsPath) throws IOException {
            Files.writeString(featurePath, featureFile);
            Files.writeString(stepsPath, stepDefinitions);
        }
    }
}
