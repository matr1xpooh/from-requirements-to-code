package an.story.gherkin_generator.model;

/**
 * Container for generated test artifacts
 */
public class TestPackage {
    private String featureFile;
    private String stepDefinitions;
    
    public TestPackage(String featureFile, String stepDefinitions) {
        this.featureFile = featureFile;
        this.stepDefinitions = stepDefinitions;
    }
    
    public String getFeatureFile() { return featureFile; }
    public String getStepDefinitions() { return stepDefinitions; }
}

