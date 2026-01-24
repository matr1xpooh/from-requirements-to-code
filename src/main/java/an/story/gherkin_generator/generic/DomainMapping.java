package an.story.gherkin_generator.generic;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for domain-specific mappings between business acceptance criteria
 * and technical API/Kafka testing steps.
 *
 * Implementations define how business language in Jira stories maps to:
 * - REST API endpoints and request structures
 * - Kafka topics and message verification
 * - Technical Gherkin steps that can be executed
 *
 * Example domains:
 * - Credit card transaction processing
 * - User registration and authentication
 * - Order management
 * - Payment processing
 */
public interface DomainMapping {

    /**
     * Get the name of this domain (e.g., "Credit Card Transactions").
     */
    String getDomainName();

    /**
     * Get the base URL pattern for the API (e.g., "http://localhost:{port}").
     */
    String getBaseUrlPattern();

    /**
     * Get the default port for the API server.
     */
    int getDefaultPort();

    /**
     * Get all API endpoints defined in this domain.
     */
    List<ApiEndpoint> getApiEndpoints();

    /**
     * Get all Kafka topics that should be monitored in this domain.
     */
    List<KafkaTopic> getKafkaTopics();

    /**
     * Get all step patterns for transforming business steps to technical steps.
     */
    List<StepPattern> getStepPatterns();

    /**
     * Find the API endpoint that handles a specific action.
     *
     * @param actionDescription Business description like "submit", "process", "create"
     * @return The matching endpoint, if found
     */
    Optional<ApiEndpoint> findEndpointForAction(String actionDescription);

    /**
     * Find the Kafka topic for a specific event type.
     *
     * @param eventDescription Business description like "notification", "event"
     * @return The matching topic, if found
     */
    Optional<KafkaTopic> findTopicForEvent(String eventDescription);

    /**
     * Transform a business step into a technical step.
     *
     * @param stepType The type of step (Given/When/Then)
     * @param businessStep The business language step text
     * @return The transformed technical step
     */
    String transformStep(StepPattern.StepType stepType, String businessStep);

    /**
     * Get the background setup steps for this domain.
     * These are added to every scenario.
     */
    List<String> getBackgroundSteps();

    /**
     * Get imports needed for step definition classes.
     */
    List<String> getStepDefinitionImports();

    /**
     * Get instance variable declarations for step definition classes.
     */
    List<String> getStepDefinitionInstanceVariables();

    /**
     * Get setup code for @Before hook in step definitions.
     */
    String getBeforeHookCode();

    /**
     * Get teardown code for @After hook in step definitions.
     */
    String getAfterHookCode();

    /**
     * Get synonyms/keywords that trigger specific actions.
     * This helps the converter understand different phrasings of the same concept.
     *
     * Example: {"submit": ["process", "send", "submit"], "approve": ["approved", "success"]}
     */
    Map<String, List<String>> getActionSynonyms();
}
