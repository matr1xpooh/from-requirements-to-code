package an.story.creditcard.main;

import an.story.creditcard.consumer.ApplicantDataCleansedConsumer;
import an.story.creditcard.model.Address;
import an.story.creditcard.model.ApplicantData;
import an.story.creditcard.service.DataCleanseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * Main class demonstrating the DataCleanseService and Consumer
 */
public class CreditCardApplicationMain {
    private static final Logger logger = LoggerFactory.getLogger(CreditCardApplicationMain.class);
    
    public static void main(String[] args) {
        // Example: Producer side
        demonstrateProducer();
        
        // Example: Consumer side (run in separate thread or separate process)
        // demonstrateConsumer();
    }
    
    /**
     * Demonstrates how to use DataCleanseService to send events
     */
    private static void demonstrateProducer() {
        DataCleanseService service = new DataCleanseService();
        
        try {
            // Create sample applicant data
            Address address = new Address(
                "123 Main Street",
                "New York",
                "NY",
                "10001"
            );
            
            ApplicantData applicantData = new ApplicantData(
                "APP-12345",
                "John Doe",
                "john.doe@example.com",
                "555-123-4567",
                LocalDate.of(1990, 5, 15),
                "123-45-6789",
                address,
                75000.0
            );
            
            // Cleanse data and send event
            boolean success = service.cleanseAndSendEvent(applicantData);
            
            if (success) {
                logger.info("Successfully cleansed data and sent event to Kafka");
            } else {
                logger.error("Failed to send event to Kafka");
            }
            
            // Wait a bit for the event to be sent
            Thread.sleep(1000);
            
        } catch (Exception e) {
            logger.error("Error in producer demonstration", e);
        } finally {
            service.close();
        }
    }
    
    /**
     * Demonstrates how to use ApplicantDataCleansedConsumer to receive events
     */
    private static void demonstrateConsumer() {
        ApplicantDataCleansedConsumer consumer = new ApplicantDataCleansedConsumer("credit-card-app-group");
        
        // Run consumer in a separate thread
        Thread consumerThread = new Thread(() -> {
            try {
                consumer.start();
            } catch (Exception e) {
                logger.error("Error in consumer thread", e);
            }
        });
        
        consumerThread.setDaemon(true);
        consumerThread.start();
        
        // Let it run for a while
        try {
            Thread.sleep(30000); // Run for 30 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            consumer.stop();
        }
    }
}

