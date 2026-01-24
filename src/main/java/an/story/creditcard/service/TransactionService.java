package an.story.creditcard.service;

import an.story.creditcard.config.KafkaConfig;
import an.story.creditcard.model.TransactionRequest;
import an.story.creditcard.model.TransactionResponse;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Service for processing credit card transactions.
 * Validates the transaction, processes payment, and publishes Kafka events.
 */
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private static final String TOPIC_TRANSACTION_PROCESSED = "credit-card-transaction-processed";

    private final KafkaProducer<String, GenericRecord> producer;
    private final Schema transactionProcessedSchema;
    private final Schema transactionTypeSchema;
    private final Schema transactionStatusSchema;

    public TransactionService() {
        this(new KafkaProducer<>(KafkaConfig.createProducerProperties()));
    }

    public TransactionService(KafkaProducer<String, GenericRecord> producer) {
        this.producer = producer;
        this.transactionProcessedSchema = loadSchema("/avro/creditCardTransactionProcessed.avsc");
        this.transactionTypeSchema = transactionProcessedSchema.getField("transactionType").schema();
        this.transactionStatusSchema = transactionProcessedSchema.getField("status").schema();
    }

    private Schema loadSchema(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Schema not found: " + resourcePath);
            }
            return new Schema.Parser().parse(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schema: " + resourcePath, e);
        }
    }

    /**
     * Process a credit card transaction.
     *
     * @param request The transaction request
     * @return The transaction response
     */
    public TransactionResponse processTransaction(TransactionRequest request) {
        String transactionId = generateTransactionId();
        logger.info("Processing transaction: {}", transactionId);

        try {
            // Validate the transaction
            ValidationResult validationResult = validateTransaction(request);

            if (!validationResult.isValid()) {
                logger.warn("Transaction {} validation failed: {}", transactionId, validationResult.getReason());
                TransactionResponse response = TransactionResponse.declined(
                    transactionId,
                    request.getMaskedCardNumber(),
                    request.getAmount(),
                    request.getCurrency(),
                    validationResult.getReason()
                );
                publishTransactionEvent(transactionId, request, "DECLINED", null, validationResult.getReason());
                return response;
            }

            // Simulate payment processing
            String authorizationCode = processPayment(request);

            // Create success response
            TransactionResponse response = TransactionResponse.approved(
                transactionId,
                authorizationCode,
                request.getMaskedCardNumber(),
                request.getAmount(),
                request.getCurrency()
            );

            // Publish Kafka event for successful transaction
            publishTransactionEvent(transactionId, request, "APPROVED", authorizationCode, null);

            logger.info("Transaction {} processed successfully with auth code: {}", transactionId, authorizationCode);
            return response;

        } catch (Exception e) {
            logger.error("Error processing transaction {}: {}", transactionId, e.getMessage(), e);
            TransactionResponse response = TransactionResponse.error(transactionId, "Internal processing error");
            publishTransactionEvent(transactionId, request, "ERROR", null, e.getMessage());
            return response;
        }
    }

    private ValidationResult validateTransaction(TransactionRequest request) {
        // Validate card number (basic Luhn check simulation)
        if (request.getCardNumber() == null || request.getCardNumber().length() < 13) {
            return ValidationResult.invalid("Invalid card number");
        }

        // Validate card holder name
        if (request.getCardHolderName() == null || request.getCardHolderName().trim().isEmpty()) {
            return ValidationResult.invalid("Card holder name is required");
        }

        // Validate expiry date
        YearMonth expiry = YearMonth.of(request.getExpiryYear(), request.getExpiryMonth());
        if (expiry.isBefore(YearMonth.now())) {
            return ValidationResult.invalid("Card has expired");
        }

        // Validate CVV
        if (request.getCvv() == null || !request.getCvv().matches("\\d{3,4}")) {
            return ValidationResult.invalid("Invalid CVV");
        }

        // Validate amount
        if (request.getAmount() <= 0) {
            return ValidationResult.invalid("Invalid transaction amount");
        }

        if (request.getAmount() > 50000) {
            return ValidationResult.invalid("Transaction amount exceeds limit");
        }

        // Validate merchant
        if (request.getMerchantId() == null || request.getMerchantId().trim().isEmpty()) {
            return ValidationResult.invalid("Merchant ID is required");
        }

        return ValidationResult.valid();
    }

    private String processPayment(TransactionRequest request) {
        // Simulate payment processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Generate authorization code
        return "AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void publishTransactionEvent(String transactionId, TransactionRequest request,
                                        String status, String authCode, String declineReason) {
        try {
            GenericRecord record = new GenericData.Record(transactionProcessedSchema);
            record.put("transactionId", transactionId);
            record.put("cardNumber", request.getMaskedCardNumber());
            record.put("cardHolderName", request.getCardHolderName());
            record.put("amount", request.getAmount());
            record.put("currency", request.getCurrency());
            record.put("merchantId", request.getMerchantId());
            record.put("merchantName", request.getMerchantName() != null ? request.getMerchantName() : "Unknown");
            record.put("transactionType", new GenericData.EnumSymbol(transactionTypeSchema,
                mapTransactionType(request.getTransactionType())));
            record.put("status", new GenericData.EnumSymbol(transactionStatusSchema, status));
            record.put("authorizationCode", authCode);
            record.put("declineReason", declineReason);
            record.put("processedTimestamp", System.currentTimeMillis());
            record.put("metadata", new HashMap<String, String>());

            ProducerRecord<String, GenericRecord> producerRecord =
                new ProducerRecord<>(TOPIC_TRANSACTION_PROCESSED, transactionId, record);

            Future<RecordMetadata> future = producer.send(producerRecord);
            RecordMetadata metadata = future.get();

            logger.info("Published transaction event to {} partition {} offset {}",
                metadata.topic(), metadata.partition(), metadata.offset());

        } catch (Exception e) {
            logger.error("Failed to publish transaction event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish transaction event", e);
        }
    }

    private String mapTransactionType(String type) {
        if (type == null) return "PURCHASE";
        return switch (type.toUpperCase()) {
            case "REFUND" -> "REFUND";
            case "AUTHORIZATION", "AUTH" -> "AUTHORIZATION";
            case "VOID" -> "VOID";
            default -> "PURCHASE";
        };
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString();
    }

    public void close() {
        if (producer != null) {
            producer.close();
        }
    }

    /**
     * Inner class to represent validation results
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String reason;

        private ValidationResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }

        static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        static ValidationResult invalid(String reason) {
            return new ValidationResult(false, reason);
        }

        boolean isValid() {
            return valid;
        }

        String getReason() {
            return reason;
        }
    }
}
