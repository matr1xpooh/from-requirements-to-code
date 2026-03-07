package an.story.creditcard.main;

import an.story.creditcard.consumer.ApplicantDataCleansedConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerMain {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerMain.class);

    public static void main(String[] args) {
        ApplicantDataCleansedConsumer consumer = new ApplicantDataCleansedConsumer("credit-card-app-group");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal received, stopping consumer...");
            consumer.stop();
        }));

        consumer.start();
    }
}
