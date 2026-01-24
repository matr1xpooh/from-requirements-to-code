{panel:title=Value Statement | titleBGColor=#b9d9ed}
As a Chase Bank operations manager, I want to ensure that credit card transactions are
processed correctly and that successful transactions trigger downstream notifications,
so that we maintain accurate transaction records and enable real-time fraud monitoring.
{panel}

{panel:title=Requirements | titleBGColor=#b9d9ed}
1. implement the "transaction processing" service to validate and process credit card transactions
2. ensure that the "creditCardTransactionProcessed" event is published when a transaction is successfully processed
3. update the avro schema for the "creditCardTransactionProcessed" event to include transaction details including status, authorization code, and decline reason
4. validate that expired cards are rejected with appropriate error messages
5. validate that transaction amounts exceeding $50,000 are declined
6. ensure that all processed transactions include a unique transaction identifier
{panel}

{panel:title=Acceptance Criteria | titleBGColor=#b9d9ed}
Scenario: Successfully process a valid credit card transaction
Given a valid credit card with number ending in 4242
And the card has not expired
And the transaction amount is $150.00
And the merchant is "Coffee Shop Inc"
When the transaction is submitted for processing
Then the transaction should be approved
And an authorization code should be generated
And a transaction processed notification should be sent
And the notification should contain the transaction ID and status

Scenario: Reject an expired credit card
Given a credit card that expired last month
And the transaction amount is $50.00
When the transaction is submitted for processing
Then the transaction should be declined
And the decline reason should indicate card expiration
And a transaction processed notification should be sent with declined status

Scenario: Reject transaction exceeding limit
Given a valid credit card with sufficient balance
And the transaction amount is $75,000.00
When the transaction is submitted for processing
Then the transaction should be declined
And the decline reason should indicate amount exceeds limit
And a transaction processed notification should be sent with declined status

Scenario: Reject transaction with invalid card number
Given a credit card with an invalid card number
And the transaction amount is $100.00
When the transaction is submitted for processing
Then the transaction should be declined
And the decline reason should indicate invalid card number

Scenario: Validate transaction notification contains required fields
Given a valid credit card transaction is processed
When the transaction processed notification is received
Then the notification should contain the masked card number
And the notification should contain the merchant information
And the notification should contain the transaction timestamp
And the notification should contain the transaction type
{panel}
