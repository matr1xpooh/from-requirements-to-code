
package an.story.creditcard.main;

import an.story.creditcard.model.Address;
import an.story.creditcard.model.ApplicantData;
import an.story.creditcard.service.DataCleanseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class ProducerMain {
    private static final Logger logger = LoggerFactory.getLogger(ProducerMain.class);

    public static void main(String[] args) {
        DataCleanseService service = new DataCleanseService();

        try {
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

            boolean success = service.cleanseAndSendEvent(applicantData);

            if (success) {
                logger.info("Successfully cleansed data and sent event to Kafka");
            } else {
                logger.error("Failed to send event to Kafka");
            }

            Thread.sleep(1000);

        } catch (Exception e) {
            logger.error("Error in producer", e);
        } finally {
            service.close();
        }
    }
}
