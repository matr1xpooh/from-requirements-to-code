package an.story.gherkin_generator.converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines mappings between business-oriented acceptance criteria steps
 * and API-aware technical steps for credit card transaction testing.
 */
public class TransactionStepMappings {

    private final List<StepMapping> givenMappings;
    private final List<StepMapping> whenMappings;
    private final List<StepMapping> thenMappings;

    public TransactionStepMappings() {
        this.givenMappings = createGivenMappings();
        this.whenMappings = createWhenMappings();
        this.thenMappings = createThenMappings();
    }

    private List<StepMapping> createGivenMappings() {
        List<StepMapping> mappings = new ArrayList<>();

        // Card setup mappings
        mappings.add(new StepMapping(
            "a valid credit card with number ending in (\\d+)",
            "I have a valid credit card with number ending in \"$1\"",
            StepMapping.StepType.GIVEN
        ));

        mappings.add(new StepMapping(
            "the card has not expired",
            "the card expiry date is set to next year",
            StepMapping.StepType.GIVEN
        ));

        mappings.add(new StepMapping(
            "a credit card that expired",
            "I have a credit card with expiry date in the past",
            StepMapping.StepType.GIVEN
        ));

        mappings.add(new StepMapping(
            "the transaction amount is \\$(\\d+[,.]?\\d*)",
            "I set the transaction amount to $1",
            StepMapping.StepType.GIVEN
        ));

        mappings.add(new StepMapping(
            "the merchant is \"([^\"]+)\"",
            "I set the merchant to \"$1\" with ID \"MERCH-001\"",
            StepMapping.StepType.GIVEN
        ));

        mappings.add(new StepMapping(
            "a valid credit card with sufficient balance",
            "I have a valid credit card with number ending in \"4242\"",
            StepMapping.StepType.GIVEN
        ));

        mappings.add(new StepMapping(
            "a credit card with an invalid card number",
            "I have a credit card with number \"123\"",
            StepMapping.StepType.GIVEN
        ));

        mappings.add(new StepMapping(
            "a valid credit card transaction is processed",
            "I have a valid credit card with number ending in \"4242\" and the card expiry date is set to next year and I set the transaction amount to 100.00",
            StepMapping.StepType.GIVEN
        ));

        return mappings;
    }

    private List<StepMapping> createWhenMappings() {
        List<StepMapping> mappings = new ArrayList<>();

        mappings.add(new StepMapping(
            "the transaction is submitted for processing",
            "I POST the transaction to \"/api/v1/transactions\"",
            StepMapping.StepType.WHEN
        ));

        mappings.add(new StepMapping(
            "the transaction processed notification is received",
            "I consume the message from topic \"credit-card-transaction-processed\"",
            StepMapping.StepType.WHEN
        ));

        return mappings;
    }

    private List<StepMapping> createThenMappings() {
        List<StepMapping> mappings = new ArrayList<>();

        mappings.add(new StepMapping(
            "the transaction should be approved",
            "the response status code should be 200 and the response field \"status\" should be \"APPROVED\"",
            StepMapping.StepType.THEN
        ));

        mappings.add(new StepMapping(
            "an authorization code should be generated",
            "the response field \"authorization_code\" should not be empty",
            StepMapping.StepType.THEN
        ));

        mappings.add(new StepMapping(
            "a transaction processed notification should be sent",
            "a message should be published to topic \"credit-card-transaction-processed\"",
            StepMapping.StepType.THEN
        ));

        mappings.add(new StepMapping(
            "the notification should contain the transaction ID and status",
            "the Kafka message field \"transactionId\" should not be empty and the Kafka message field \"status\" should not be empty",
            StepMapping.StepType.THEN
        ));

        mappings.add(new StepMapping(
            "the transaction should be declined",
            "the response status code should be 422 and the response field \"status\" should be \"DECLINED\"",
            StepMapping.StepType.THEN
        ));

        mappings.add(new StepMapping(
            "the decline reason should indicate card expiration",
            "the response field \"decline_reason\" should contain \"expired\"",
            StepMapping.StepType.THEN
        ));

        mappings.add(new StepMapping(
            "the decline reason should indicate amount exceeds limit",
            "the response field \"decline_reason\" should contain \"exceeds limit\"",
            StepMapping.StepType.THEN
        ));

        mappings.add(new StepMapping(
            "the decline reason should indicate invalid card number",
            "the response field \"decline_reason\" should contain \"Invalid card number\"",
            StepMapping.StepType.THEN
        ));

        mappings.add(new StepMapping(
            "a transaction processed notification should be sent with declined status",
            "a message should be published to topic \"credit-card-transaction-processed\" with field \"status\" equal to \"DECLINED\"",
            StepMapping.StepType.THEN
        ));

        mappings.add(new StepMapping(
            "the notification should contain the masked card number",
            "the Kafka message field \"cardNumber\" should match \"\\*\\*\\*\\* \\*\\*\\*\\* \\*\\*\\*\\* \\d{4}\"",
            StepMapping.StepType.THEN
        ));

        mappings.add(new StepMapping(
            "the notification should contain the merchant information",
            "the Kafka message field \"merchantId\" should not be empty and the Kafka message field \"merchantName\" should not be empty",
            StepMapping.StepType.THEN
        ));

        mappings.add(new StepMapping(
            "the notification should contain the transaction timestamp",
            "the Kafka message field \"processedTimestamp\" should be greater than 0",
            StepMapping.StepType.THEN
        ));

        mappings.add(new StepMapping(
            "the notification should contain the transaction type",
            "the Kafka message field \"transactionType\" should not be empty",
            StepMapping.StepType.THEN
        ));

        return mappings;
    }

    public String transformGiven(String businessStep) {
        for (StepMapping mapping : givenMappings) {
            if (mapping.matches(businessStep)) {
                return mapping.transform(businessStep);
            }
        }
        // Return original if no mapping found (with a comment)
        return businessStep + " # TODO: Implement API mapping";
    }

    public String transformWhen(String businessStep) {
        for (StepMapping mapping : whenMappings) {
            if (mapping.matches(businessStep)) {
                return mapping.transform(businessStep);
            }
        }
        return businessStep + " # TODO: Implement API mapping";
    }

    public String transformThen(String businessStep) {
        for (StepMapping mapping : thenMappings) {
            if (mapping.matches(businessStep)) {
                return mapping.transform(businessStep);
            }
        }
        return businessStep + " # TODO: Implement API mapping";
    }

    public List<StepMapping> getAllMappings() {
        List<StepMapping> all = new ArrayList<>();
        all.addAll(givenMappings);
        all.addAll(whenMappings);
        all.addAll(thenMappings);
        return all;
    }
}
