package an.story.creditcard.consumer;

import an.story.creditcard.config.KafkaConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Consumer for aoaApplicantDataCleansed events from Kafka
 */
public class ApplicantDataCleansedConsumer {
    private static final Logger logger = LoggerFactory.getLogger(ApplicantDataCleansedConsumer.class);
    private static final String TOPIC_NAME = "aoa-applicant-data-cleansed";
    
    private final Consumer<String, Object> consumer;
    private volatile boolean running = true;
    
    public ApplicantDataCleansedConsumer(String groupId) {
        Properties props = KafkaConfig.createConsumerProperties(groupId);
        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(TOPIC_NAME));
    }
    
    public ApplicantDataCleansedConsumer(Consumer<String, Object> consumer) {
        this.consumer = consumer;
        this.consumer.subscribe(Collections.singletonList(TOPIC_NAME));
    }
    
    /**
     * Starts consuming events from Kafka
     */
    public void start() {
        logger.info("Starting consumer for topic: {}", TOPIC_NAME);
        
        try {
            while (running) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(100));
                
                for (ConsumerRecord<String, Object> record : records) {
                    processEvent(record);
                }
            }
        } catch (Exception e) {
            logger.error("Error while consuming events", e);
        } finally {
            consumer.close();
            logger.info("Consumer closed");
        }
    }
    
    /**
     * Processes a single event record
     */
    private void processEvent(ConsumerRecord<String, Object> record) {
        try {
            logger.info("Received event - Key: {}, Partition: {}, Offset: {}", 
                record.key(), record.partition(), record.offset());
            
            // Extract data from Avro event
            Object avroEvent = record.value();
            if (avroEvent != null) {
                processAvroEvent(avroEvent);
            } else {
                logger.warn("Received null event value");
            }
        } catch (Exception e) {
            logger.error("Error processing event", e);
        }
    }
    
    /**
     * Processes the Avro event object
     */
    private void processAvroEvent(Object avroEvent) {
        try {
            // Use reflection to extract data from Avro object
            Class<?> avroClass = avroEvent.getClass();
            
            String applicationId = (String) avroClass.getMethod("getApplicationId").invoke(avroEvent);
            String applicantName = (String) avroClass.getMethod("getApplicantName").invoke(avroEvent);
            String email = (String) avroClass.getMethod("getEmail").invoke(avroEvent);
            String phoneNumber = (String) avroClass.getMethod("getPhoneNumber").invoke(avroEvent);
            String dateOfBirth = (String) avroClass.getMethod("getDateOfBirth").invoke(avroEvent);
            String ssn = (String) avroClass.getMethod("getSsn").invoke(avroEvent);
            Long cleansedTimestamp = (Long) avroClass.getMethod("getCleansedTimestamp").invoke(avroEvent);
            Object cleansingStatus = avroClass.getMethod("getCleansingStatus").invoke(avroEvent);
            
            // Get address
            Object addressObj = avroClass.getMethod("getAddress").invoke(avroEvent);
            String addressStr = null;
            if (addressObj != null) {
                Class<?> addressClass = addressObj.getClass();
                String street = (String) addressClass.getMethod("getStreet").invoke(addressObj);
                String city = (String) addressClass.getMethod("getCity").invoke(addressObj);
                String state = (String) addressClass.getMethod("getState").invoke(addressObj);
                String zipCode = (String) addressClass.getMethod("getZipCode").invoke(addressObj);
                addressStr = String.format("%s, %s, %s %s", street, city, state, zipCode);
            }
            
            // Get annual income
            Double annualIncome = (Double) avroClass.getMethod("getAnnualIncome").invoke(avroEvent);
            
            logger.info("Processing credit card application event:");
            logger.info("  Application ID: {}", applicationId);
            logger.info("  Applicant Name: {}", applicantName);
            logger.info("  Email: {}", email);
            logger.info("  Phone: {}", phoneNumber);
            logger.info("  Date of Birth: {}", dateOfBirth);
            logger.info("  SSN: {}", ssn);
            logger.info("  Address: {}", addressStr);
            logger.info("  Annual Income: {}", annualIncome);
            logger.info("  Cleansed Timestamp: {}", cleansedTimestamp);
            logger.info("  Cleansing Status: {}", cleansingStatus);
            
            // Here you would typically call business logic to process the application
            // For example: validateApplication(applicationId, applicantName, ...);
            
        } catch (Exception e) {
            logger.error("Error extracting data from Avro event", e);
        }
    }
    
    /**
     * Stops the consumer
     */
    public void stop() {
        running = false;
        logger.info("Stopping consumer...");
    }
    
    /**
     * Closes the consumer
     */
    public void close() {
        stop();
        if (consumer != null) {
            consumer.close();
        }
    }
}

