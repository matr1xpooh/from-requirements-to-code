package an.story.gherkin_generator.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines a pattern for matching business-language steps and transforming them
 * into API-aware technical steps.
 *
 * Supports regex capture groups for extracting values from business steps
 * and inserting them into the technical steps.
 */
public class StepPattern {

    public enum StepType {
        GIVEN, WHEN, THEN
    }

    private final StepType stepType;
    private final Pattern businessPattern;
    private final String technicalTemplate;
    private final List<String> setupActions;
    private final List<FieldMapping> fieldMappings;

    private StepPattern(StepType stepType, String businessPattern, String technicalTemplate) {
        this.stepType = stepType;
        this.businessPattern = Pattern.compile(businessPattern, Pattern.CASE_INSENSITIVE);
        this.technicalTemplate = technicalTemplate;
        this.setupActions = new ArrayList<>();
        this.fieldMappings = new ArrayList<>();
    }

    public static StepPattern given(String businessPattern, String technicalTemplate) {
        return new StepPattern(StepType.GIVEN, businessPattern, technicalTemplate);
    }

    public static StepPattern when(String businessPattern, String technicalTemplate) {
        return new StepPattern(StepType.WHEN, businessPattern, technicalTemplate);
    }

    public static StepPattern then(String businessPattern, String technicalTemplate) {
        return new StepPattern(StepType.THEN, businessPattern, technicalTemplate);
    }

    /**
     * Add a field mapping that sets a request field when this pattern matches.
     */
    public StepPattern setsField(String fieldName, String valueTemplate) {
        fieldMappings.add(new FieldMapping(fieldName, valueTemplate));
        return this;
    }

    /**
     * Add a setup action that should be executed when this pattern matches.
     */
    public StepPattern withSetupAction(String action) {
        setupActions.add(action);
        return this;
    }

    public boolean matches(String businessStep) {
        return businessPattern.matcher(businessStep.trim()).matches();
    }

    /**
     * Transform a business step into a technical step.
     * Captures groups from the business step and inserts them into the technical template.
     */
    public String transform(String businessStep) {
        Matcher matcher = businessPattern.matcher(businessStep.trim());
        if (!matcher.matches()) {
            return businessStep;
        }

        String result = technicalTemplate;
        for (int i = 1; i <= matcher.groupCount(); i++) {
            String captured = matcher.group(i);
            if (captured != null) {
                // Clean up captured values (remove $ signs, commas from amounts, etc.)
                captured = cleanCapturedValue(captured);
                result = result.replace("$" + i, captured);
            }
        }

        return result;
    }

    private String cleanCapturedValue(String value) {
        // Remove $ signs and commas from monetary amounts
        if (value.matches("\\$?[\\d,]+\\.?\\d*")) {
            return value.replace("$", "").replace(",", "");
        }
        return value;
    }

    /**
     * Extract field values from a matched business step.
     */
    public List<FieldMapping> getFieldMappings(String businessStep) {
        Matcher matcher = businessPattern.matcher(businessStep.trim());
        if (!matcher.matches()) {
            return new ArrayList<>();
        }

        List<FieldMapping> resolved = new ArrayList<>();
        for (FieldMapping mapping : fieldMappings) {
            String value = mapping.getValueTemplate();
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String captured = matcher.group(i);
                if (captured != null) {
                    captured = cleanCapturedValue(captured);
                    value = value.replace("$" + i, captured);
                }
            }
            resolved.add(new FieldMapping(mapping.getFieldName(), value));
        }

        return resolved;
    }

    public StepType getStepType() {
        return stepType;
    }

    public List<String> getSetupActions() {
        return setupActions;
    }

    public Pattern getBusinessPattern() {
        return businessPattern;
    }

    public String getTechnicalTemplate() {
        return technicalTemplate;
    }

    /**
     * Represents a mapping from a captured value to a request field.
     */
    public static class FieldMapping {
        private final String fieldName;
        private final String valueTemplate;

        public FieldMapping(String fieldName, String valueTemplate) {
            this.fieldName = fieldName;
            this.valueTemplate = valueTemplate;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getValueTemplate() {
            return valueTemplate;
        }
    }
}
