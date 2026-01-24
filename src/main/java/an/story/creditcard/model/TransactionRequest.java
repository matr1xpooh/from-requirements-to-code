package an.story.creditcard.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request model for credit card transaction processing
 */
public class TransactionRequest {

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("card_holder_name")
    private String cardHolderName;

    @JsonProperty("expiry_month")
    private int expiryMonth;

    @JsonProperty("expiry_year")
    private int expiryYear;

    @JsonProperty("cvv")
    private String cvv;

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("currency")
    private String currency = "USD";

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("merchant_name")
    private String merchantName;

    @JsonProperty("transaction_type")
    private String transactionType = "PURCHASE";

    public TransactionRequest() {
    }

    public TransactionRequest(String cardNumber, String cardHolderName, int expiryMonth,
                             int expiryYear, String cvv, double amount, String merchantId,
                             String merchantName) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvv = cvv;
        this.amount = amount;
        this.merchantId = merchantId;
        this.merchantName = merchantName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public int getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(int expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public int getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(int expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
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

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
