# Auto-generated from Jira acceptance criteria
# This feature file contains API-aware scenarios for testing the Transaction REST API
# and verifying Kafka message publication

Feature: Credit card transactions are processed correctly and successful transactions trigger downstream notifications
  As a Chase Bank operations manager, I want to ensure that credit card transactions are processed correctly and that successful transactions trigger downstream notifications, so that we maintain accurate transaction records and enable real-time fraud monitoring.

  Background: API and Kafka test infrastructure
    Given the Transaction API is running on port 8080
    And a Kafka consumer is listening on topic "credit-card-transaction-processed"

  Scenario: Successfully process a valid credit card transaction
    Given I have a valid credit card with number ending in "4242"
    And the card expiry date is set to next year
    And I set the transaction amount to 150.00
    And I set the merchant to "Coffee Shop Inc" with ID "MERCH-001"
    When I POST the transaction to "/api/v1/transactions"
    Then the response status code should be 200 and the response field "status" should be "APPROVED"
    And the response field "authorization_code" should not be empty
    And a message should be published to topic "credit-card-transaction-processed"
    And the Kafka message field "transactionId" should not be empty and the Kafka message field "status" should not be empty

  Scenario: Reject an expired credit card
    Given I have a credit card with expiry date in the past
    And I set the transaction amount to 50.00
    When I POST the transaction to "/api/v1/transactions"
    Then the response status code should be 422 and the response field "status" should be "DECLINED"
    And the response field "decline_reason" should contain "expired"
    And a message should be published to topic "credit-card-transaction-processed" with field "status" equal to "DECLINED"

  Scenario: Reject transaction exceeding limit
    Given I have a valid credit card with number ending in "4242"
    And the card expiry date is set to next year
    And I set the transaction amount to 75000.00
    When I POST the transaction to "/api/v1/transactions"
    Then the response status code should be 422 and the response field "status" should be "DECLINED"
    And the response field "decline_reason" should contain "exceeds limit"
    And a message should be published to topic "credit-card-transaction-processed" with field "status" equal to "DECLINED"

  Scenario: Reject transaction with invalid card number
    Given I have a credit card with number "123"
    And I set the transaction amount to 100.00
    When I POST the transaction to "/api/v1/transactions"
    Then the response status code should be 422 and the response field "status" should be "DECLINED"
    And the response field "decline_reason" should contain "Invalid card number"

  Scenario: Validate transaction notification contains required fields
    Given I have a valid credit card with number ending in "4242"
    And the card expiry date is set to next year
    And I set the transaction amount to 100.00
    And I set the merchant to "Test Merchant" with ID "MERCH-TEST"
    When I POST the transaction to "/api/v1/transactions"
    And I consume the message from topic "credit-card-transaction-processed"
    Then the Kafka message field "cardNumber" should match "\\*\\*\\*\\* \\*\\*\\*\\* \\*\\*\\*\\* \\d{4}"
    And the Kafka message field "merchantId" should not be empty and the Kafka message field "merchantName" should not be empty
    And the Kafka message field "processedTimestamp" should be greater than 0
    And the Kafka message field "transactionType" should not be empty
