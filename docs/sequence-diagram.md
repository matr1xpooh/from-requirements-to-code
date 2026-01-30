# Codebase Sequence Diagrams

This document contains sequence diagrams illustrating the main flows in the "From Requirements to Code" system.

## Overview

The codebase consists of three main subsystems:
1. **Story Parsing & Test Generation** - Parses Jira stories and generates Gherkin test files
2. **Credit Card Processing (Kafka)** - An event-driven system for processing credit card applications
3. **Test Execution Framework** - A test harness for running acceptance tests

---

## Flow 1: Jira Story Parsing & Test Generation

This flow shows how a Jira story is parsed and transformed into a Gherkin feature file.

```mermaid
sequenceDiagram
    participant TGM as TestGeneratorMain
    participant JSP as JiraStoryParser
    participant GTG as GherkinTestGenerator
    participant GFFSC as GenerateFeatureFileFromStoryCommand
    participant GFNC as GenerateFeatureNameCommand
    participant GBC as GenerateBackgroundCommand
    participant GSC as GenerateScenarioCommand
    participant FS as FileSystem

    TGM->>JSP: parse(storyText)
    activate JSP
    JSP->>JSP: extractPanels(storyText)
    JSP->>JSP: parseValueStatement(panelContent)
    JSP->>JSP: parseRequirements(panelContent)
    JSP->>JSP: parseAcceptanceCriteria(panelContent)
    JSP-->>TGM: JiraStory
    deactivate JSP

    TGM->>GTG: generateFeatureFileFromStory(story)
    activate GTG
    GTG->>GFFSC: execute()
    activate GFFSC

    GFFSC->>GFNC: execute()
    activate GFNC
    GFNC-->>GFFSC: "Feature: <name>"
    deactivate GFNC

    GFFSC->>JSP: extractTopology(story)
    activate JSP
    JSP-->>GFFSC: ServiceTopology
    deactivate JSP

    GFFSC->>GBC: execute(topology)
    activate GBC
    GBC-->>GFFSC: "Background: ..."
    deactivate GBC

    loop for each AcceptanceCriterion
        GFFSC->>GSC: execute(criterion)
        activate GSC
        GSC-->>GFFSC: "Scenario: ..."
        deactivate GSC
    end

    GFFSC-->>GTG: featureFileContent
    deactivate GFFSC

    GTG-->>TGM: featureFileContent
    deactivate GTG

    TGM->>FS: write to target/generated-tests/
```

---

## Flow 2: Step Definitions Generation

This flow shows how step definition Java classes are generated from a parsed Jira story.

```mermaid
sequenceDiagram
    participant GTG as GherkinTestGenerator
    participant GSDC as GenerateStepDefinitionsCommand
    participant CUSC as CollectUniqueStepsCommand
    participant GSMC as GenerateStepMethodCommand
    participant DAC as DetermineAnnotationCommand
    participant GMNC as GenerateMethodNameCommand

    GTG->>GSDC: execute()
    activate GSDC

    GSDC->>GSDC: generatePackageDeclaration()
    GSDC->>GSDC: generateImports()
    GSDC->>GSDC: generateClassHeader()

    GSDC->>CUSC: execute()
    activate CUSC
    Note right of CUSC: Extract all unique<br/>Given/When/Then steps
    CUSC-->>GSDC: List<String> uniqueSteps
    deactivate CUSC

    loop for each step
        GSDC->>GSMC: execute(step)
        activate GSMC

        GSMC->>DAC: execute(step)
        activate DAC
        DAC-->>GSMC: @Given/@When/@Then
        deactivate DAC

        GSMC->>GMNC: execute(step)
        activate GMNC
        GMNC-->>GSMC: methodName
        deactivate GMNC

        GSMC-->>GSDC: stepMethodCode
        deactivate GSMC
    end

    GSDC-->>GTG: stepDefinitionsClass
    deactivate GSDC
```

---

## Flow 3: Credit Card Data Cleansing & Kafka Event Publishing

This flow shows how applicant data is cleansed and published to Kafka.

```mermaid
sequenceDiagram
    participant CCAM as CreditCardApplicationMain
    participant DCS as DataCleanseService
    participant KC as KafkaConfig
    participant KP as KafkaProducer
    participant KB as Kafka Broker

    CCAM->>CCAM: create ApplicantData with Address
    CCAM->>DCS: new DataCleanseService()
    activate DCS
    DCS->>KC: createProducerProperties()
    KC-->>DCS: Properties
    DCS->>KP: new KafkaProducer(props)
    deactivate DCS

    CCAM->>DCS: cleanseAndSendEvent(applicantData)
    activate DCS

    DCS->>DCS: cleanseData(applicantData)
    Note right of DCS: - Normalize name (trim, spacing)<br/>- Lowercase email<br/>- Remove non-numeric from phone<br/>- Mask SSN (XXX-XX-1234)<br/>- Normalize address
    DCS-->>DCS: cleansedApplicantData

    DCS->>DCS: createAvroEvent(cleansedData)
    Note right of DCS: Uses reflection to create<br/>AoaApplicantDataCleansed<br/>Avro object
    DCS-->>DCS: avroEvent

    DCS->>KP: send(ProducerRecord)
    activate KP
    KP->>KB: publish to "aoa-applicant-data-cleansed"
    KB-->>KP: ack
    KP-->>DCS: RecordMetadata (async callback)
    deactivate KP

    DCS-->>CCAM: true (success)
    deactivate DCS
```

---

## Flow 4: Credit Card Event Consumption

This flow shows how events are consumed from Kafka by the consumer service.

```mermaid
sequenceDiagram
    participant CCAM as CreditCardApplicationMain
    participant ADCC as ApplicantDataCleansedConsumer
    participant KC as KafkaConfig
    participant KCC as KafkaConsumer
    participant KB as Kafka Broker

    CCAM->>ADCC: new ApplicantDataCleansedConsumer(groupId)
    activate ADCC
    ADCC->>KC: createConsumerProperties(groupId)
    KC-->>ADCC: Properties
    ADCC->>KCC: new KafkaConsumer(props)
    ADCC->>KCC: subscribe("aoa-applicant-data-cleansed")
    deactivate ADCC

    CCAM->>ADCC: start()
    activate ADCC

    loop while running
        ADCC->>KCC: poll(Duration.ofMillis(100))
        activate KCC
        KCC->>KB: fetch records
        KB-->>KCC: ConsumerRecords
        KCC-->>ADCC: ConsumerRecords
        deactivate KCC

        loop for each record
            ADCC->>ADCC: processEvent(record)
            ADCC->>ADCC: processAvroEvent(avroEvent)
            Note right of ADCC: Uses reflection to<br/>extract Avro fields
        end
    end

    CCAM->>ADCC: stop()
    ADCC->>KCC: close()
    deactivate ADCC
```

---

## Flow 5: Test Execution with Test Harness

This flow shows how acceptance tests are executed using the MultiServiceTestHarness.

```mermaid
sequenceDiagram
    participant CR as Cucumber Runner
    participant SD as StepDefinitions
    participant MSTH as MultiServiceTestHarness
    participant SC as ServiceClient
    participant EBS as EventBusSpy
    participant TC as TestContext

    rect rgb(200, 230, 200)
        Note over CR,TC: Setup Phase
        CR->>SD: @Before setUp()
        activate SD
        SD->>MSTH: new MultiServiceTestHarness()
        SD->>MSTH: setup()
        activate MSTH

        loop for each service in topology
            MSTH->>SC: new ServiceClient(serviceName)
            MSTH->>MSTH: serviceClients.put(name, client)
        end

        loop for each event in topology
            MSTH->>EBS: subscribe(eventType)
        end

        MSTH-->>SD: ready
        deactivate MSTH

        SD->>TC: setHarness(harness)
        deactivate SD
    end

    rect rgb(200, 200, 230)
        Note over CR,TC: Test Execution Phase
        CR->>SD: @Given step
        activate SD
        SD->>TC: getHarness()
        SD->>MSTH: getService(serviceName)
        activate MSTH
        MSTH-->>SD: ServiceClient
        deactivate MSTH
        SD->>SC: invokeAPI(request)
        SC-->>SD: response
        deactivate SD

        CR->>SD: @When step
        activate SD
        SD->>SC: performAction(data)
        SC-->>SD: result
        deactivate SD

        CR->>SD: @Then step
        activate SD
        SD->>MSTH: waitForEvent(eventType, timeout)
        activate MSTH
        MSTH->>EBS: waitForEvent(eventType, timeout)
        activate EBS
        EBS-->>MSTH: Event
        deactivate EBS
        MSTH-->>SD: Event
        deactivate MSTH

        SD->>MSTH: verifyEventNotPublished(eventType)
        activate MSTH
        MSTH->>EBS: hasEvent(eventType)
        EBS-->>MSTH: false
        MSTH-->>SD: verified
        deactivate MSTH
        deactivate SD
    end

    rect rgb(230, 200, 200)
        Note over CR,TC: Teardown Phase
        CR->>SD: @After tearDown()
        activate SD
        SD->>TC: getHarness()
        SD->>MSTH: teardown()
        activate MSTH
        MSTH->>SC: close()
        MSTH->>EBS: close()
        MSTH-->>SD: done
        deactivate MSTH
        deactivate SD
    end
```

---

## Component Relationships

```mermaid
graph TB
    subgraph "Story Parsing & Test Generation"
        TGM[TestGeneratorMain] --> JSP[JiraStoryParser]
        JSP --> JS[JiraStory]
        JS --> VS[ValueStatement]
        JS --> REQ[Requirement]
        JS --> AC[AcceptanceCriterion]
        TGM --> GTG[GherkinTestGenerator]
        GTG --> CMD[Command Pattern]
        CMD --> FF[Feature File]
        CMD --> SDF[Step Definitions]
    end

    subgraph "Credit Card Processing"
        CCAM[CreditCardApplicationMain] --> DCS[DataCleanseService]
        DCS --> AD[ApplicantData]
        DCS --> KP[KafkaProducer]
        KP --> KT[(Kafka Topic)]
        KT --> KC[KafkaConsumer]
        KC --> ADCC[ApplicantDataCleansedConsumer]
    end

    subgraph "Test Execution Framework"
        CUC[Cucumber] --> SD[StepDefinitions]
        SD --> MSTH[MultiServiceTestHarness]
        MSTH --> SVC[ServiceClient]
        MSTH --> EBS[EventBusSpy]
        MSTH --> SRC[SchemaRegistryClient]
        SD --> TC[TestContext]
    end
```

---

## Key Files

| Component | File Path |
|-----------|-----------|
| TestGeneratorMain | `src/main/java/an/story/main/TestGeneratorMain.java` |
| JiraStoryParser | `src/main/java/an/story/parser/JiraStoryParser.java` |
| GherkinTestGenerator | `src/main/java/an/story/generator/GherkinTestGenerator.java` |
| DataCleanseService | `src/main/java/an/story/creditcard/service/DataCleanseService.java` |
| ApplicantDataCleansedConsumer | `src/main/java/an/story/creditcard/consumer/ApplicantDataCleansedConsumer.java` |
| MultiServiceTestHarness | `src/main/java/an/story/framework/MultiServiceTestHarness.java` |
| KafkaConfig | `src/main/java/an/story/creditcard/config/KafkaConfig.java` |
| Avro Schema | `src/main/resources/avro/aoaApplicantDataCleansed.avsc` |
