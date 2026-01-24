package an.story.gherkin_generator.converter;

import java.util.regex.Pattern;

/**
 * Represents a mapping from a business-oriented step pattern to an API-aware technical step.
 */
public class StepMapping {

    private final Pattern businessPattern;
    private final String technicalStep;
    private final StepType stepType;

    public StepMapping(String businessPatternRegex, String technicalStep, StepType stepType) {
        this.businessPattern = Pattern.compile(businessPatternRegex, Pattern.CASE_INSENSITIVE);
        this.technicalStep = technicalStep;
        this.stepType = stepType;
    }

    public boolean matches(String businessStep) {
        return businessPattern.matcher(businessStep).find();
    }

    public String transform(String businessStep) {
        java.util.regex.Matcher matcher = businessPattern.matcher(businessStep);
        if (matcher.find()) {
            String result = technicalStep;
            for (int i = 1; i <= matcher.groupCount(); i++) {
                result = result.replace("$" + i, matcher.group(i) != null ? matcher.group(i) : "");
            }
            return result;
        }
        return technicalStep;
    }

    public Pattern getBusinessPattern() {
        return businessPattern;
    }

    public String getTechnicalStep() {
        return technicalStep;
    }

    public StepType getStepType() {
        return stepType;
    }

    public enum StepType {
        GIVEN, WHEN, THEN
    }
}
