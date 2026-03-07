package an.story.creditcard.main;

/**
 * Entry point that runs both producer and consumer as daemon threads.
 * To run them independently use ProducerMain or ConsumerMain.
 */
public class CreditCardApplicationMain {

    public static void main(String[] args) throws InterruptedException {
        Thread consumerThread = startDaemon("consumer", () -> ConsumerMain.main(new String[]{}));

        // Give the consumer a moment to subscribe before the producer sends
        Thread.sleep(2000);

        startDaemon("producer", () -> ProducerMain.main(args));

        // Keep the main thread alive while daemons run
        consumerThread.join();
    }

    private static Thread startDaemon(String name, Runnable task) {
        Thread thread = new Thread(task, name);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
}
