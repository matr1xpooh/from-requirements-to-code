# Gherkin Test Generator Guide

## Overview

The Gherkin Test Generator automatically creates complete Cucumber test suites from parsed Jira stories, including feature files and Java step definitions with embedded test harness setup.

## Architecture

### Command Pattern

All generators implement the `Command<T>` interface:

```java
public interface Command<T> {
    T execute();
}
```

This provides:
- **Composability**: Commands can call other commands
- **Testability**: Each command can be tested in isolation
- **Clarity**: Clear input/output contracts

### Generator Pipeline

```
JiraStory
    ↓
extractTopologyStatic() → ServiceTopology
    ↓
┌─────────────────────────┬────────────────────────────┐
│                         │                            │
GenerateFeatureFile      GenerateStepDefinitions      │
    ↓                         ↓                        │
Feature Name            Step Definition Class Name    │
Background              Unique Steps Collection       │
Scenarios               Individual Step Methods       │
    ↓                         ↓                        │
└─────────────────────────┴────────────────────────────┘
                           ↓
                   TestPackage
                (feature + steps + paths)
```

---

## Feature File Generation

### GenerateFeatureFileFromStoryCommand

Generates a complete `.feature` file from a story.

#### Input
```java
JiraStory story = parser.parse(storyText);
GenerateFeatureFileFromStoryCommand cmd =
    new GenerateFeatureFileFromStoryCommand(story);
```

#### Output
```gherkin
Feature: {Feature Name}
  {Value Statement}

  Background:
    Given the following services are running:
      | Service1 |
      | Service2 |

  Scenario: {Scenario 1}
    Given {precondition}
    When {action}
    Then {result}

  Scenario: {Scenario 2}
    ...
```

#### Example

**Input Story**:
```
{panel:title=Value Statement}
As a developer, I want automated testing, so that I can deploy faster
{panel}

{panel:title=Requirements}
1. The "OrderService" service processes orders
2. The "PaymentService" service handles payments
{panel}

{panel:title=Acceptance Criteria}
Scenario: Process valid order
  Given an order with valid items
  When the order is submitted
  Then the order is processed successfully
{panel}
```

**Generated Feature**:
```gherkin
Feature: Automated Testing
  As a developer, I want automated testing, so that I can deploy faster

  Background:
    Given the following services are running:
      | OrderService |
      | PaymentService |

  Scenario: Process valid order
    Given an order with valid items
    When the order is submitted
    Then the order is processed successfully
```

### Feature Name Generation

**GenerateFeatureNameCommand**

Extracts feature name from value statement goal.

**Rules**:
1. Take the goal from value statement
2. Capitalize first letter of each word
3. Remove "to" prefix if present

**Examples**:
```java
// "I want to parse stories" → "Parse Stories"
// "I want automated testing" → "Automated Testing"
// "I want to generate test code" → "Generate Test Code"
```

**Code**:
```java
GenerateFeatureNameCommand cmd = new GenerateFeatureNameCommand(story);
String featureName = cmd.execute();
```

### Background Generation

**GenerateBackgroundCommand**

Creates background section with service initialization.

**Input**: `ServiceTopology` (extracted from requirements)

**Output**: Background section or empty string if no services

**Examples**:

**With Services**:
```gherkin
  Background:
    Given the following services are running:
      | OrderService |
      | PaymentService |
      | NotificationService |
```

**No Services**:
```gherkin
(empty - no background section)
```

**Code**:
```java
ServiceTopology topology = JiraStoryParser.extractTopologyStatic(story);
GenerateBackgroundCommand cmd = new GenerateBackgroundCommand(topology);
String background = cmd.execute();
```

### Scenario Generation

**GenerateScenarioCommand**

Converts acceptance criterion to Gherkin scenario.

**Input**: `AcceptanceCriterion`

**Output**: Complete scenario block

**Format**:
```gherkin
  Scenario: {Scenario Name}
    Given {given statement}
    When {when statement}
    Then {then statement}
```

**Example**:
```java
AcceptanceCriterion ac = story.getAcceptanceCriteria().get(0);
GenerateScenarioCommand cmd = new GenerateScenarioCommand(ac);
String scenario = cmd.execute();
```

**Generated**:
```gherkin
  Scenario: Process valid order
    Given an order with valid items
    When the order is submitted
    Then the order is processed successfully
```

---

## Step Definition Generation

### GenerateStepDefinitionsCommand

Generates complete Java step definition class.

#### Input
```java
JiraStory story = parser.parse(storyText);
GenerateStepDefinitionsCommand cmd =
    new GenerateStepDefinitionsCommand(story, "com.example.steps");
```

#### Output Structure
```java
package com.example.steps;

import io.cucumber.java.en.*;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import static org.junit.jupiter.api.Assertions.*;
import an.story.gherkin_generator.TestContext;
import an.story.gherkin_generator.MultiServiceTestHarness;
import java.util.Arrays;
import java.util.List;

public class AutomatedTestingSteps {

    private TestContext context;
    private MultiServiceTestHarness harness;

    public AutomatedTestingSteps(TestContext context) {
        this.context = context;
    }

    @Before
    public void setUp() throws Exception {
        List<String> services = Arrays.asList("OrderService", "PaymentService");
        List<String> events = Arrays.asList("OrderCreated", "PaymentProcessed");
        harness = new MultiServiceTestHarness(services, events);
        harness.setup();
        context.setHarness(harness);
    }

    @After
    public void tearDown() throws Exception {
        if (harness != null) {
            harness.teardown();
        }
    }

    @Given("an order with valid items")
    public void anOrderWithValidItems() {
        // TODO: implement step
    }

    @When("the order is submitted")
    public void theOrderIsSubmitted() {
        // TODO: implement step
    }

    @Then("the order is processed successfully")
    public void theOrderIsProcessedSuccessfully() {
        // TODO: implement step
    }
}
```

### Class Name Generation

**GenerateStepDefinitionClassNameCommand**

Creates class name from value statement goal.

**Rules**:
1. Extract goal from value statement
2. Convert to PascalCase
3. Append "Steps" suffix
4. Remove "to" if present

**Examples**:
```java
// "parse stories" → "ParseStoriesSteps"
// "automated testing" → "AutomatedTestingSteps"
// "generate test code" → "GenerateTestCodeSteps"
```

**Code**:
```java
GenerateStepDefinitionClassNameCommand cmd =
    new GenerateStepDefinitionClassNameCommand(story);
String className = cmd.execute();
```

### Unique Step Collection

**CollectUniqueStepsCommand**

Extracts all unique steps from acceptance criteria.

**Input**: `JiraStory` with acceptance criteria

**Output**: `Set<String>` of unique steps

**Deduplication**: Same step text across multiple scenarios generates only one method

**Example**:

**Input**:
```
Scenario: Valid login
  Given the user is on login page
  When the user enters credentials
  Then the user is logged in

Scenario: Invalid login
  Given the user is on login page
  When the user enters wrong credentials
  Then an error is shown
```

**Output**:
```java
Set<String> uniqueSteps = {
    "the user is on login page",      // Appears in both scenarios
    "the user enters credentials",
    "the user is logged in",
    "the user enters wrong credentials",
    "an error is shown"
}
// Only 5 step methods generated, not 6
```

**Code**:
```java
CollectUniqueStepsCommand cmd = new CollectUniqueStepsCommand(story);
Set<String> steps = cmd.execute();
```

### Step Method Generation

**GenerateStepMethodCommand**

Generates a single step method from step text.

**Input**: Step text (e.g., "an order with valid items")

**Output**: Complete method with annotation

#### Annotation Determination

**DetermineAnnotationCommand**

Maps step text to Cucumber annotation based on first word.

**Rules**:
- Starts with "the", "a", "an": `@Given`
- Otherwise: `@Given` (default)

**Note**: Current implementation defaults to `@Given`. In practice, you may need to track step type from the original Given/When/Then context.

#### Method Name Generation

**GenerateMethodNameCommand**

Converts step text to valid Java method name.

**Rules**:
1. Convert to camelCase
2. Remove articles ("the", "a", "an")
3. Remove special characters
4. Start with lowercase letter

**Examples**:
```java
// "an order with valid items" → "orderWithValidItems"
// "the order is submitted" → "orderIsSubmitted"
// "the order is processed successfully" → "orderIsProcessedSuccessfully"
// "3 items are in the cart" → "itemsAreInTheCart"
```

**Code**:
```java
GenerateMethodNameCommand cmd = new GenerateMethodNameCommand(stepText);
String methodName = cmd.execute();
```

#### Complete Method Generation

**Example**:

**Input**: `"an order with valid items"`

**Output**:
```java
    @Given("an order with valid items")
    public void anOrderWithValidItems() {
        // TODO: implement step
    }
```

**Code**:
```java
GenerateStepMethodCommand cmd = new GenerateStepMethodCommand(stepText);
String method = cmd.execute();
```

---

## Complete Test Package Generation

### GenerateCompleteTestPackageCommand

Orchestrates full test generation: feature file + step definitions + file writing.

#### Input
```java
JiraStory story = parser.parse(storyText);
GenerateCompleteTestPackageCommand cmd =
    new GenerateCompleteTestPackageCommand(
        story,
        "com.example",       // Base package
        "target/generated"   // Output directory
    );
```

#### Output: TestPackage
```java
public class TestPackage {
    private String featureFilePath;
    private String featureFileContent;
    private String stepDefinitionsFilePath;
    private String stepDefinitionsContent;
}
```

#### File Structure

**Generated Files**:
```
target/generated/
├── features/
│   └── AutomatedTesting.feature
└── com/example/steps/
    └── AutomatedTestingSteps.java
```

#### Example Usage

```java
JiraStory story = parser.parse(storyText);
GenerateCompleteTestPackageCommand cmd =
    new GenerateCompleteTestPackageCommand(story, "com.example", "target/generated");
TestPackage pkg = cmd.execute();

System.out.println("Feature: " + pkg.getFeatureFilePath());
System.out.println("Steps: " + pkg.getStepDefinitionsFilePath());

// Files are automatically written to disk
```

#### File Paths

**Feature File Path**:
```
{outputDir}/features/{FeatureName}.feature
```

**Step Definitions Path**:
```
{outputDir}/{package/path}/steps/{ClassName}.java
```

---

## Test Harness Integration

### MultiServiceTestHarness

Embedded test infrastructure for multi-service testing.

#### Features

1. **Embedded Kafka**: In-memory Kafka broker (no Docker needed)
2. **Service Management**: Start/stop services
3. **Event Spy**: Capture and verify events
4. **Schema Validation**: Avro schema support

#### Generated Setup Code

```java
@Before
public void setUp() throws Exception {
    List<String> services = Arrays.asList("OrderService", "PaymentService");
    List<String> events = Arrays.asList("OrderCreated", "PaymentProcessed");
    harness = new MultiServiceTestHarness(services, events);
    harness.setup();  // Starts Kafka, initializes services
    context.setHarness(harness);
}

@After
public void tearDown() throws Exception {
    if (harness != null) {
        harness.teardown();  // Stops services, shuts down Kafka
    }
}
```

#### Usage in Steps

```java
@When("an order is created")
public void anOrderIsCreated() {
    MultiServiceTestHarness harness = context.getHarness();

    // Use service client
    ServiceClient orderService = harness.getServiceClient("OrderService");
    orderService.sendRequest("/orders", orderData);

    // Verify event published
    EventBusSpy spy = harness.getEventSpy();
    assertTrue(spy.hasEvent("OrderCreated"));
}
```

### TestContext

Shared state container injected into step definitions.

#### Constructor Injection

Cucumber automatically injects TestContext:

```java
public class AutomatedTestingSteps {
    private TestContext context;

    public AutomatedTestingSteps(TestContext context) {
        this.context = context;
    }
}
```

#### Storing State

```java
@Given("an order with id {string}")
public void anOrderWithId(String orderId) {
    context.setHarness(harness);
    context.put("orderId", orderId);  // Store for later steps
}

@When("the order is retrieved")
public void theOrderIsRetrieved() {
    String orderId = (String) context.get("orderId");
    // Use stored orderId
}
```

---

## Customization

### Custom Step Method Templates

Override `GenerateStepMethodCommand` for custom templates:

```java
public class CustomStepMethodCommand extends GenerateStepMethodCommand {
    @Override
    public String execute() {
        String method = super.execute();
        // Add custom assertions or setup
        return method.replace("// TODO: implement step",
                            "// TODO: implement step\nassertNotNull(context);");
    }
}
```

### Custom Annotation Logic

Implement custom annotation determination:

```java
public class SmartAnnotationCommand implements Command<String> {
    private String stepText;

    @Override
    public String execute() {
        if (stepText.contains("is") || stepText.contains("has")) {
            return "@Given";
        } else if (stepText.contains("submit") || stepText.contains("click")) {
            return "@When";
        } else if (stepText.contains("see") || stepText.contains("receive")) {
            return "@Then";
        }
        return "@Given";
    }
}
```

### Custom Feature Templates

Create custom feature file format:

```java
public class CustomFeatureCommand implements Command<String> {
    private JiraStory story;

    @Override
    public String execute() {
        StringBuilder feature = new StringBuilder();
        feature.append("@automated\n");  // Add tag
        feature.append("Feature: ").append(getFeatureName()).append("\n");
        // ... custom format
        return feature.toString();
    }
}
```

---

## Best Practices

### 1. Organize Generated Tests

**Recommended Structure**:
```
src/test/java/
└── com/example/
    ├── steps/
    │   ├── OrderProcessingSteps.java
    │   ├── PaymentProcessingSteps.java
    │   └── NotificationSteps.java
    └── runners/
        └── CucumberTest.java

src/test/resources/
└── features/
    ├── OrderProcessing.feature
    ├── PaymentProcessing.feature
    └── Notification.feature
```

### 2. Implement Step Methods

Generated step methods are stubs. Implement them:

```java
@Given("an order with valid items")
public void anOrderWithValidItems() {
    Order order = new Order();
    order.addItem("ITEM-001", 2);
    order.addItem("ITEM-002", 1);
    context.put("order", order);
}
```

### 3. Use Test Harness Effectively

```java
@When("the order is submitted")
public void theOrderIsSubmitted() {
    MultiServiceTestHarness harness = context.getHarness();
    Order order = (Order) context.get("order");

    // Send to service
    ServiceClient client = harness.getServiceClient("OrderService");
    client.submitOrder(order);

    // Wait for event
    EventBusSpy spy = harness.getEventSpy();
    spy.waitForEvent("OrderCreated", 5000);  // 5 second timeout
}
```

### 4. Reuse Step Definitions

Same step text across features → Single step method:

```gherkin
Feature: Order Processing
  Scenario: Valid order
    Given the user is logged in
    When an order is submitted

Feature: Payment Processing
  Scenario: Payment
    Given the user is logged in  # Reuses same step method
    When payment is processed
```

### 5. Parameterize Steps

Use Cucumber expressions:

```java
@Given("an order with {int} items")
public void anOrderWithItems(int itemCount) {
    Order order = new Order();
    for (int i = 0; i < itemCount; i++) {
        order.addItem("ITEM-" + i, 1);
    }
    context.put("order", order);
}
```

---

## Testing Generated Code

### Unit Testing Generators

Test each command in isolation:

```java
@Test
public void testFeatureNameGeneration() {
    ValueStatement vs = new ValueStatement(
        "developer", "parse stories", "automate testing");
    JiraStory story = new JiraStory(vs, List.of(), List.of());

    GenerateFeatureNameCommand cmd = new GenerateFeatureNameCommand(story);
    String name = cmd.execute();

    assertEquals("Parse Stories", name);
}
```

### Integration Testing

Test complete generation pipeline:

```java
@Test
public void testCompleteGeneration() throws Exception {
    String storyText = loadSampleStory();
    JiraStory story = parser.parse(storyText);

    GenerateCompleteTestPackageCommand cmd =
        new GenerateCompleteTestPackageCommand(story, "com.test", "target/test");
    TestPackage pkg = cmd.execute();

    assertTrue(Files.exists(Paths.get(pkg.getFeatureFilePath())));
    assertTrue(Files.exists(Paths.get(pkg.getStepDefinitionsFilePath())));

    String featureContent = pkg.getFeatureFileContent();
    assertTrue(featureContent.contains("Feature:"));
    assertTrue(featureContent.contains("Scenario:"));
}
```

### Running Generated Tests

```bash
# Run Cucumber tests
mvn test -Dtest=CucumberTest

# Run specific feature
mvn test -Dcucumber.filter.tags="@order-processing"

# Generate HTML report
mvn test -Dcucumber.plugin="html:target/cucumber-reports"
```

---

## Troubleshooting

### Common Issues

**Issue**: Generated step methods have wrong annotations

**Solution**: Track original step type (Given/When/Then) and use in generation

**Issue**: Duplicate step methods

**Solution**: Use `CollectUniqueStepsCommand` to deduplicate

**Issue**: Test harness fails to start

**Solution**: Check port availability, Kafka configuration

**Issue**: Generated code doesn't compile

**Solution**: Verify package names, imports, Java version

---

## Examples

### Complete Example

See [API Reference](API.md) for complete end-to-end example.

### Sample Generated Test

**Feature File** (`OrderProcessing.feature`):
```gherkin
Feature: Order Processing
  As a customer, I want to process orders, so that I can purchase items

  Background:
    Given the following services are running:
      | OrderService |
      | PaymentService |

  Scenario: Valid order
    Given an order with 3 items
    When the order is submitted
    Then the order is processed successfully
    And an "OrderCreated" event is published
```

**Step Definitions** (`OrderProcessingSteps.java`):
```java
package com.example.steps;

import io.cucumber.java.en.*;
import an.story.gherkin_generator.TestContext;
import an.story.gherkin_generator.MultiServiceTestHarness;

public class OrderProcessingSteps {
    private TestContext context;
    private MultiServiceTestHarness harness;

    public OrderProcessingSteps(TestContext context) {
        this.context = context;
    }

    @Before
    public void setUp() throws Exception {
        harness = new MultiServiceTestHarness(
            Arrays.asList("OrderService", "PaymentService"),
            Arrays.asList("OrderCreated"));
        harness.setup();
        context.setHarness(harness);
    }

    @Given("an order with {int} items")
    public void anOrderWithItems(int itemCount) {
        // Implementation
    }

    @When("the order is submitted")
    public void theOrderIsSubmitted() {
        // Implementation
    }

    @Then("the order is processed successfully")
    public void theOrderIsProcessedSuccessfully() {
        // Implementation
    }

    @Then("an {string} event is published")
    public void anEventIsPublished(String eventName) {
        // Implementation
    }

    @After
    public void tearDown() throws Exception {
        if (harness != null) {
            harness.teardown();
        }
    }
}
```

---

## Future Enhancements

### Planned Features
- **Scenario Outline support**: Generate parameterized tests
- **Custom templates**: Pluggable template engine
- **Multiple languages**: Generate Python, JavaScript step definitions
- **Smart assertions**: Auto-generate assertions based on Then statements
- **Data table support**: Generate step methods with DataTable parameters

For more information, see [Architecture Guide](ARCHITECTURE.md) and [Development Guide](DEVELOPMENT.md).
