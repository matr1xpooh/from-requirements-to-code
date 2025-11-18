package an.story.gherkin_generator.model;

import java.util.Optional;

/**
 * Container for generated test artifacts
 */
public class TestPackage {
    private String featureFile;
    private String stepDefinitions;

    public TestPackage(String featureFile) {
        this(featureFile, null);    
    }
    
    public TestPackage(String featureFile, String stepDefinitions) {
        this.featureFile = featureFile;
        this.stepDefinitions = stepDefinitions;
    }
    
    public String getFeatureFile() { return featureFile; }
    
    /**
     * Returns the step definitions if present, otherwise empty Optional
     * @return Optional containing step definitions if generated, empty otherwise
     */
    public Optional<String> getStepDefinitions() {
        return Optional.ofNullable(stepDefinitions);
    }
    
    /**
     * Checks if step definitions were generated
     * @return true if step definitions are present, false otherwise
     */
    public boolean hasStepDefinitions() {
        return stepDefinitions != null;
    }
}

