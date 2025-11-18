package an.story.creditcard.service;

import an.story.creditcard.config.KafkaConfig;
import an.story.creditcard.model.Address;
import an.story.creditcard.model.ApplicantData;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Service that cleanses applicant data and sends events to Kafka
 */
public class DataCleanseService {
    private static final Logger logger = LoggerFactory.getLogger(DataCleanseService.class);
    private static final String TOPIC_NAME = "aoa-applicant-data-cleansed";
    
    private final Producer<String, Object> producer;
    
    public DataCleanseService() {
        Properties props = KafkaConfig.createProducerProperties();
        this.producer = new KafkaProducer<>(props);
    }
    
    public DataCleanseService(Producer<String, Object> producer) {
        this.producer = producer;
    }
    
    /**
     * Cleanses applicant data and sends an event to Kafka
     * 
     * @param applicantData The raw applicant data to cleanse
     * @return true if the event was sent successfully, false otherwise
     */
    public boolean cleanseAndSendEvent(ApplicantData applicantData) {
        try {
            // Perform data cleansing
            ApplicantData cleansedData = cleanseData(applicantData);
            
            // Create Avro event object
            // Note: This uses the generated Avro class from the schema
            // The class will be generated at: an.story.creditcard.events.AoaApplicantDataCleansed
            Object avroEvent = createAvroEvent(cleansedData);
            
            // Send event to Kafka
            ProducerRecord<String, Object> record = new ProducerRecord<>(
                TOPIC_NAME,
                cleansedData.getApplicationId(),
                avroEvent
            );
            
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    logger.error("Error sending event to Kafka", exception);
                } else {
                    logger.info("Event sent successfully to topic: {}, partition: {}, offset: {}", 
                        metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
            
            return true;
        } catch (Exception e) {
            logger.error("Error during data cleansing and event sending", e);
            return false;
        }
    }
    
    /**
     * Cleanses the applicant data (normalizes, validates, etc.)
     */
    private ApplicantData cleanseData(ApplicantData data) {
        ApplicantData cleansed = new ApplicantData();
        
        // Copy and cleanse data
        cleansed.setApplicationId(data.getApplicationId());
        cleansed.setApplicantName(normalizeName(data.getApplicantName()));
        cleansed.setEmail(normalizeEmail(data.getEmail()));
        cleansed.setPhoneNumber(normalizePhone(data.getPhoneNumber()));
        cleansed.setDateOfBirth(data.getDateOfBirth());
        cleansed.setSsn(maskSsn(data.getSsn()));
        cleansed.setAddress(normalizeAddress(data.getAddress()));
        cleansed.setAnnualIncome(data.getAnnualIncome());
        
        return cleansed;
    }
    
    /**
     * Creates an Avro event object from cleansed applicant data
     * Note: This method uses reflection to work with the generated Avro class
     */
    private Object createAvroEvent(ApplicantData data) {
        try {
            // Get the generated Avro class
            Class<?> avroClass = Class.forName("an.story.creditcard.events.AoaApplicantDataCleansed");
            
            // Create builder
            Object builder = avroClass.getMethod("newBuilder").invoke(null);
            
            // Set fields using builder pattern
            builder.getClass().getMethod("setApplicationId", String.class).invoke(builder, data.getApplicationId());
            builder.getClass().getMethod("setApplicantName", String.class).invoke(builder, data.getApplicantName());
            builder.getClass().getMethod("setEmail", String.class).invoke(builder, data.getEmail());
            
            if (data.getPhoneNumber() != null) {
                builder.getClass().getMethod("setPhoneNumber", String.class).invoke(builder, data.getPhoneNumber());
            }
            
            builder.getClass().getMethod("setDateOfBirth", String.class)
                .invoke(builder, data.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE));
            
            if (data.getSsn() != null) {
                builder.getClass().getMethod("setSsn", String.class).invoke(builder, data.getSsn());
            }
            
            // Create address Avro object
            if (data.getAddress() != null) {
                Class<?> addressClass = Class.forName("an.story.creditcard.events.Address");
                Object addressBuilder = addressClass.getMethod("newBuilder").invoke(null);
                addressBuilder.getClass().getMethod("setStreet", String.class)
                    .invoke(addressBuilder, data.getAddress().getStreet());
                addressBuilder.getClass().getMethod("setCity", String.class)
                    .invoke(addressBuilder, data.getAddress().getCity());
                addressBuilder.getClass().getMethod("setState", String.class)
                    .invoke(addressBuilder, data.getAddress().getState());
                addressBuilder.getClass().getMethod("setZipCode", String.class)
                    .invoke(addressBuilder, data.getAddress().getZipCode());
                Object address = addressBuilder.getClass().getMethod("build").invoke(addressBuilder);
                builder.getClass().getMethod("setAddress", addressClass).invoke(builder, address);
            }
            
            if (data.getAnnualIncome() != null) {
                builder.getClass().getMethod("setAnnualIncome", Double.class).invoke(builder, data.getAnnualIncome());
            }
            
            builder.getClass().getMethod("setCleansedTimestamp", Long.class)
                .invoke(builder, System.currentTimeMillis());
            
            // Set cleansing status
            Class<?> statusEnum = Class.forName("an.story.creditcard.events.CleansingStatus");
            Object successStatus = Enum.valueOf((Class<Enum>) statusEnum, "SUCCESS");
            builder.getClass().getMethod("setCleansingStatus", statusEnum).invoke(builder, successStatus);
            
            // Build and return the Avro object
            return builder.getClass().getMethod("build").invoke(builder);
        } catch (Exception e) {
            logger.error("Error creating Avro event object", e);
            throw new RuntimeException("Failed to create Avro event", e);
        }
    }
    
    private String normalizeName(String name) {
        if (name == null) return null;
        return name.trim().replaceAll("\\s+", " ");
    }
    
    private String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }
    
    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[^0-9]", "");
    }
    
    private String maskSsn(String ssn) {
        if (ssn == null) return null;
        // Mask SSN: show only last 4 digits
        if (ssn.length() >= 4) {
            return "XXX-XX-" + ssn.substring(ssn.length() - 4);
        }
        return ssn;
    }
    
    private Address normalizeAddress(Address address) {
        if (address == null) return null;
        Address normalized = new Address();
        normalized.setStreet(address.getStreet() != null ? address.getStreet().trim() : null);
        normalized.setCity(address.getCity() != null ? address.getCity().trim() : null);
        normalized.setState(address.getState() != null ? address.getState().trim().toUpperCase() : null);
        normalized.setZipCode(address.getZipCode() != null ? address.getZipCode().trim() : null);
        return normalized;
    }
    
    /**
     * Closes the Kafka producer
     */
    public void close() {
        if (producer != null) {
            producer.close();
            logger.info("Kafka producer closed");
        }
    }
}

