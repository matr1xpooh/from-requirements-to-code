package an.story.creditcard.main;

import an.story.creditcard.controller.TransactionController;
import an.story.creditcard.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application to start the Credit Card Transaction REST API server.
 *
 * This server exposes:
 * - POST /api/v1/transactions - Process a credit card transaction
 * - GET /api/v1/health - Health check endpoint
 *
 * On successful transaction processing, a Kafka message is published
 * to the "credit-card-transaction-processed" topic.
 */
public class TransactionServerMain {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServerMain.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        // Allow port override via command line or environment variable
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid port argument '{}', using default port {}", args[0], DEFAULT_PORT);
            }
        } else {
            String envPort = System.getenv("SERVER_PORT");
            if (envPort != null) {
                try {
                    port = Integer.parseInt(envPort);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid SERVER_PORT '{}', using default port {}", envPort, DEFAULT_PORT);
                }
            }
        }

        logger.info("Starting Credit Card Transaction API server...");

        try {
            TransactionService service = new TransactionService();
            TransactionController controller = new TransactionController(service);
            controller.start(port);

            logger.info("===========================================");
            logger.info("Credit Card Transaction API is running!");
            logger.info("Base URL: http://localhost:{}", port);
            logger.info("Endpoints:");
            logger.info("  POST /api/v1/transactions - Process transaction");
            logger.info("  GET  /api/v1/health       - Health check");
            logger.info("===========================================");
            logger.info("Press Ctrl+C to stop the server");

            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down server...");
                controller.stop();
                logger.info("Server stopped.");
            }));

            // Keep the main thread alive
            Thread.currentThread().join();

        } catch (Exception e) {
            logger.error("Failed to start server: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
