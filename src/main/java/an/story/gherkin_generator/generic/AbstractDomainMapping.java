package an.story.gherkin_generator.generic;

import java.util.*;

/**
 * Abstract base class for domain mappings providing common functionality.
 * Subclasses should override the abstract methods to define domain-specific behavior.
 */
public abstract class AbstractDomainMapping implements DomainMapping {

    protected final List<ApiEndpoint> endpoints = new ArrayList<>();
    protected final List<KafkaTopic> topics = new ArrayList<>();
    protected final List<StepPattern> stepPatterns = new ArrayList<>();
    protected final Map<String, List<String>> actionSynonyms = new HashMap<>();

    @Override
    public List<ApiEndpoint> getApiEndpoints() {
        return Collections.unmodifiableList(endpoints);
    }

    @Override
    public List<KafkaTopic> getKafkaTopics() {
        return Collections.unmodifiableList(topics);
    }

    @Override
    public List<StepPattern> getStepPatterns() {
        return Collections.unmodifiableList(stepPatterns);
    }

    @Override
    public Map<String, List<String>> getActionSynonyms() {
        return Collections.unmodifiableMap(actionSynonyms);
    }

    @Override
    public Optional<ApiEndpoint> findEndpointForAction(String actionDescription) {
        String normalizedAction = normalizeAction(actionDescription);

        for (ApiEndpoint endpoint : endpoints) {
            String endpointDesc = endpoint.getDescription().toLowerCase();
            if (endpointDesc.contains(normalizedAction)) {
                return Optional.of(endpoint);
            }
            // Check synonyms
            for (Map.Entry<String, List<String>> entry : actionSynonyms.entrySet()) {
                if (entry.getValue().stream().anyMatch(s -> normalizedAction.contains(s.toLowerCase()))) {
                    if (endpointDesc.contains(entry.getKey())) {
                        return Optional.of(endpoint);
                    }
                }
            }
        }

        // Fallback: return first POST endpoint for actions like "submit", "create"
        if (normalizedAction.matches(".*(submit|create|process|send).*")) {
            return endpoints.stream()
                    .filter(e -> "POST".equals(e.getMethod()))
                    .findFirst();
        }

        return Optional.empty();
    }

    @Override
    public Optional<KafkaTopic> findTopicForEvent(String eventDescription) {
        String normalized = eventDescription.toLowerCase();

        for (KafkaTopic topic : topics) {
            String topicName = topic.getTopicName().toLowerCase();
            String schemaName = topic.getSchemaName().toLowerCase();

            if (normalized.contains(topicName) || normalized.contains(schemaName)) {
                return Optional.of(topic);
            }

            // Check for keywords like "notification", "event", "message"
            if (normalized.contains("notification") || normalized.contains("event") ||
                normalized.contains("message") || normalized.contains("sent")) {
                return Optional.of(topic);
            }
        }

        // Return first topic if event is mentioned but not specific
        if (!topics.isEmpty() && (normalized.contains("notification") ||
                normalized.contains("event") || normalized.contains("message"))) {
            return Optional.of(topics.get(0));
        }

        return Optional.empty();
    }

    @Override
    public String transformStep(StepPattern.StepType stepType, String businessStep) {
        // Try to find a matching pattern
        for (StepPattern pattern : stepPatterns) {
            if (pattern.getStepType() == stepType && pattern.matches(businessStep)) {
                return pattern.transform(businessStep);
            }
        }

        // No pattern found - try to generate a reasonable default
        return generateDefaultTransformation(stepType, businessStep);
    }

    /**
     * Generate a default transformation when no pattern matches.
     * This uses heuristics to create reasonable technical steps.
     */
    protected String generateDefaultTransformation(StepPattern.StepType stepType, String businessStep) {
        String normalized = businessStep.toLowerCase().trim();

        switch (stepType) {
            case GIVEN:
                return transformDefaultGiven(businessStep, normalized);
            case WHEN:
                return transformDefaultWhen(businessStep, normalized);
            case THEN:
                return transformDefaultThen(businessStep, normalized);
            default:
                return businessStep + " # TODO: implement step";
        }
    }

    protected String transformDefaultGiven(String original, String normalized) {
        // Try to extract values and create setup steps
        if (normalized.contains("amount")) {
            String amount = extractAmount(original);
            if (amount != null) {
                return "I set the request field \"amount\" to " + amount;
            }
        }

        if (normalized.contains("valid")) {
            return "I have valid test data for the request";
        }

        if (normalized.contains("invalid")) {
            return "I have invalid test data for the request";
        }

        return original + " # TODO: implement step";
    }

    protected String transformDefaultWhen(String original, String normalized) {
        // Look for action keywords
        Optional<ApiEndpoint> endpoint = findEndpointForAction(normalized);

        if (endpoint.isPresent()) {
            return "I " + endpoint.get().getMethod() + " to \"" + endpoint.get().getPath() + "\"";
        }

        if (normalized.contains("submit") || normalized.contains("process") ||
            normalized.contains("send") || normalized.contains("request")) {
            // Find a POST endpoint
            return endpoints.stream()
                    .filter(e -> "POST".equals(e.getMethod()))
                    .findFirst()
                    .map(e -> "I POST to \"" + e.getPath() + "\"")
                    .orElse(original + " # TODO: implement step");
        }

        return original + " # TODO: implement step";
    }

    protected String transformDefaultThen(String original, String normalized) {
        // Success/approval patterns
        if (normalized.contains("approved") || normalized.contains("success")) {
            return "the response status should be 200 and the response field \"status\" should indicate success";
        }

        // Decline/rejection patterns
        if (normalized.contains("declined") || normalized.contains("rejected") ||
            normalized.contains("fail")) {
            return "the response status should be 422 and the response field \"status\" should indicate failure";
        }

        // Kafka/notification patterns
        if (normalized.contains("notification") || normalized.contains("event") ||
            normalized.contains("message") || normalized.contains("sent")) {
            Optional<KafkaTopic> topic = findTopicForEvent(normalized);
            if (topic.isPresent()) {
                return "a message should be published to topic \"" + topic.get().getTopicName() + "\"";
            }
        }

        // Validation/contains patterns
        if (normalized.contains("should contain") || normalized.contains("should have") ||
            normalized.contains("should include")) {
            return "the response should contain the expected fields";
        }

        // Error message patterns
        if (normalized.contains("error") || normalized.contains("reason")) {
            return "the response field \"message\" should contain the error details";
        }

        return original + " # TODO: implement step";
    }

    protected String extractAmount(String text) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$?([\\d,]+\\.?\\d*)");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        }
        return null;
    }

    protected String normalizeAction(String action) {
        return action.toLowerCase()
                .replaceAll("[^a-z\\s]", "")
                .trim();
    }

    /**
     * Register an API endpoint.
     */
    protected void registerEndpoint(ApiEndpoint endpoint) {
        endpoints.add(endpoint);
    }

    /**
     * Register a Kafka topic.
     */
    protected void registerTopic(KafkaTopic topic) {
        topics.add(topic);
    }

    /**
     * Register a step pattern.
     */
    protected void registerPattern(StepPattern pattern) {
        stepPatterns.add(pattern);
    }

    /**
     * Register action synonyms.
     */
    protected void registerSynonyms(String baseAction, String... synonyms) {
        actionSynonyms.put(baseAction, Arrays.asList(synonyms));
    }
}
