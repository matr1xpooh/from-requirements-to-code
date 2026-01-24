package an.story.creditcard.controller;

import an.story.creditcard.model.TransactionRequest;
import an.story.creditcard.model.TransactionResponse;
import an.story.creditcard.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for credit card transaction processing.
 * Exposes endpoints for processing transactions and checking status.
 */
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;
    private Javalin app;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Start the REST server on the specified port.
     *
     * @param port The port to listen on
     * @return This controller instance
     */
    public TransactionController start(int port) {
        app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        }).start(port);

        registerRoutes();

        logger.info("Transaction API started on port {}", port);
        return this;
    }

    private void registerRoutes() {
        // POST /api/v1/transactions - Process a new transaction
        app.post("/api/v1/transactions", this::processTransaction);

        // GET /api/v1/health - Health check endpoint
        app.get("/api/v1/health", ctx -> {
            ctx.json(new HealthResponse("UP", System.currentTimeMillis()));
        });
    }

    private void processTransaction(Context ctx) {
        try {
            TransactionRequest request = ctx.bodyAsClass(TransactionRequest.class);

            logger.info("Received transaction request for merchant: {}", request.getMerchantId());

            // Validate required fields
            if (request.getCardNumber() == null || request.getCardNumber().isEmpty()) {
                ctx.status(400).json(TransactionResponse.error(null, "Card number is required"));
                return;
            }

            if (request.getAmount() <= 0) {
                ctx.status(400).json(TransactionResponse.error(null, "Invalid amount"));
                return;
            }

            // Process the transaction
            TransactionResponse response = transactionService.processTransaction(request);

            // Set appropriate HTTP status based on result
            if (response.isApproved()) {
                ctx.status(200).json(response);
            } else if ("DECLINED".equals(response.getStatus())) {
                ctx.status(422).json(response);
            } else {
                ctx.status(500).json(response);
            }

        } catch (Exception e) {
            logger.error("Error processing transaction request: {}", e.getMessage(), e);
            ctx.status(500).json(TransactionResponse.error(null, "Internal server error"));
        }
    }

    /**
     * Stop the REST server.
     */
    public void stop() {
        if (app != null) {
            app.stop();
            logger.info("Transaction API stopped");
        }
        if (transactionService != null) {
            transactionService.close();
        }
    }

    /**
     * Get the base URL of the running server.
     *
     * @return The base URL
     */
    public String getBaseUrl() {
        return "http://localhost:" + app.port();
    }

    /**
     * Health response record
     */
    private record HealthResponse(String status, long timestamp) {}
}
