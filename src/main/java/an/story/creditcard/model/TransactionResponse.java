package an.story.creditcard.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for credit card transaction processing
 */
public class TransactionResponse {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("authorization_code")
    private String authorizationCode;

    @JsonProperty("masked_card_number")
    private String maskedCardNumber;

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("message")
    private String message;

    @JsonProperty("decline_reason")
    private String declineReason;

    @JsonProperty("processed_at")
    private long processedAt;

    public TransactionResponse() {
    }

    public static TransactionResponse approved(String transactionId, String authorizationCode,
                                               String maskedCardNumber, double amount, String currency) {
        TransactionResponse response = new TransactionResponse();
        response.transactionId = transactionId;
        response.status = "APPROVED";
        response.authorizationCode = authorizationCode;
        response.maskedCardNumber = maskedCardNumber;
        response.amount = amount;
        response.currency = currency;
        response.message = "Transaction approved successfully";
        response.processedAt = System.currentTimeMillis();
        return response;
    }

    public static TransactionResponse declined(String transactionId, String maskedCardNumber,
                                               double amount, String currency, String declineReason) {
        TransactionResponse response = new TransactionResponse();
        response.transactionId = transactionId;
        response.status = "DECLINED";
        response.maskedCardNumber = maskedCardNumber;
        response.amount = amount;
        response.currency = currency;
        response.declineReason = declineReason;
        response.message = "Transaction declined: " + declineReason;
        response.processedAt = System.currentTimeMillis();
        return response;
    }

    public static TransactionResponse error(String transactionId, String message) {
        TransactionResponse response = new TransactionResponse();
        response.transactionId = transactionId;
        response.status = "ERROR";
        response.message = message;
        response.processedAt = System.currentTimeMillis();
        return response;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }

    public long getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(long processedAt) {
        this.processedAt = processedAt;
    }

    public boolean isApproved() {
        return "APPROVED".equals(status);
    }
}
