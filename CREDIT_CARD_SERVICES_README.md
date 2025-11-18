# Credit Card Application Services

This module implements a Kafka-based event-driven system for processing credit card applications with data cleansing.

## Components

### 1. DataCleanseService
- **Location**: `an.story.creditcard.service.DataCleanseService`
- **Purpose**: Cleanses applicant data and publishes events to Kafka
- **Event Type**: `aoaApplicantDataCleansed`
- **Topic**: `aoa-applicant-data-cleansed`

### 2. ApplicantDataCleansedConsumer
- **Location**: `an.story.creditcard.consumer.ApplicantDataCleansedConsumer`
- **Purpose**: Consumes `aoaApplicantDataCleansed` events from Kafka
- **Topic**: `aoa-applicant-data-cleansed`

### 3. Avro Schema
- **Location**: `src/main/resources/avro/aoaApplicantDataCleansed.avsc`
- **Event Type**: `aoaApplicantDataCleansed`
- **Namespace**: `an.story.creditcard.events`

## Prerequisites

1. **Kafka**: Running Kafka broker (default: `localhost:9092`)
2. **Schema Registry**: Confluent Schema Registry (default: `http://localhost:8081`)
3. **Maven**: To build the project and generate Avro classes

## Building the Project

The Avro schema will be automatically compiled to Java classes during the Maven build:

```bash
mvn clean compile
```

This will generate the Avro classes in `target/generated-sources/avro/an/story/creditcard/events/`.

## Configuration

Kafka configuration is managed in `KafkaConfig`:
- **Bootstrap Servers**: `localhost:9092` (configurable)
- **Schema Registry URL**: `http://localhost:8081` (configurable)

## Usage

### Producer Example

```java
import an.story.creditcard.service.DataCleanseService;
import an.story.creditcard.model.ApplicantData;
import an.story.creditcard.model.Address;
import java.time.LocalDate;

DataCleanseService service = new DataCleanseService();

Address address = new Address("123 Main St", "New York", "NY", "10001");
ApplicantData applicant = new ApplicantData(
    "APP-12345",
    "John Doe",
    "john.doe@example.com",
    "555-123-4567",
    LocalDate.of(1990, 5, 15),
    "123-45-6789",
    address,
    75000.0
);

service.cleanseAndSendEvent(applicant);
service.close();
```

### Consumer Example

```java
import an.story.creditcard.consumer.ApplicantDataCleansedConsumer;

ApplicantDataCleansedConsumer consumer = new ApplicantDataCleansedConsumer("my-consumer-group");
consumer.start(); // This blocks and processes events
```

## Data Cleansing

The `DataCleanseService` performs the following data cleansing operations:
- **Name**: Trims whitespace and normalizes spacing
- **Email**: Converts to lowercase and trims
- **Phone**: Removes non-numeric characters
- **SSN**: Masks to show only last 4 digits (XXX-XX-1234)
- **Address**: Normalizes street, city, state (uppercase), and zip code

## Event Schema

The `aoaApplicantDataCleansed` event contains:
- `applicationId` (string): Unique application identifier
- `applicantName` (string): Applicant's name
- `email` (string): Email address
- `phoneNumber` (string, nullable): Phone number
- `dateOfBirth` (string): Date of birth in ISO format
- `ssn` (string, nullable): Masked SSN
- `address` (Address record): Full address
- `annualIncome` (double, nullable): Annual income
- `cleansedTimestamp` (long): Timestamp when cleansing completed
- `cleansingStatus` (enum): SUCCESS, PARTIAL, or FAILED

## Running the Example

See `CreditCardApplicationMain` for a complete example of both producer and consumer usage.

```bash
mvn exec:java -Dexec.mainClass="an.story.creditcard.main.CreditCardApplicationMain"
```

## Notes

- The implementation uses reflection to work with generated Avro classes
- After building, you can refactor to use the generated classes directly for better type safety
- The consumer runs in a blocking loop; in production, consider using a framework like Spring Kafka
- Error handling and retry logic can be enhanced based on your requirements

