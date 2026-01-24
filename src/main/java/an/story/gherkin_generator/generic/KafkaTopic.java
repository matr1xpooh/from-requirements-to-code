package an.story.gherkin_generator.generic;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Kafka topic that should be verified during testing.
 * Includes information about expected message fields and the Avro schema.
 */
public class KafkaTopic {

    private final String topicName;
    private final String schemaName;
    private final List<String> requiredFields;
    private final List<String> optionalFields;

    public KafkaTopic(String topicName, String schemaName) {
        this.topicName = topicName;
        this.schemaName = schemaName;
        this.requiredFields = new ArrayList<>();
        this.optionalFields = new ArrayList<>();
    }

    public KafkaTopic withRequiredField(String fieldName) {
        this.requiredFields.add(fieldName);
        return this;
    }

    public KafkaTopic withRequiredFields(String... fieldNames) {
        for (String field : fieldNames) {
            this.requiredFields.add(field);
        }
        return this;
    }

    public KafkaTopic withOptionalField(String fieldName) {
        this.optionalFields.add(fieldName);
        return this;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public List<String> getRequiredFields() {
        return requiredFields;
    }

    public List<String> getOptionalFields() {
        return optionalFields;
    }

    @Override
    public String toString() {
        return topicName + " (" + schemaName + ")";
    }
}
