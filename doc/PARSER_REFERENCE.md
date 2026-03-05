# Parser Reference

## Overview

This document provides a complete reference for the Jira story parser, including input format specifications, parsing rules, and examples.

## Input Format

### Jira Panel Syntax

Stories use Atlassian's `{panel}` markup format:

```
{panel:title=Panel Title}
Panel content goes here
{panel}
```

**Required Syntax**:
- Opening tag: `{panel:title=TITLE}`
- Content: Any text
- Closing tag: `{panel}`

**Optional Parameters**:
- Additional panel parameters are ignored
- Example: `{panel:title=Title|borderStyle=solid|borderColor=#ccc}`

### Story Structure

A complete story contains **three panels**:

1. **Value Statement** (Required)
2. **Requirements** (Optional)
3. **Acceptance Criteria** (Optional)

#### Complete Example

```
{panel:title=Value Statement}
As a product owner, I want to automate test generation from stories, so that I can reduce manual testing effort
{panel}

{panel:title=Requirements}
1. The "TestGenerator" service must parse Jira stories
2. The system should extract service names from requirements
3. The "StoryParsed" event must be published to Kafka with Avro schema
{panel}

{panel:title=Acceptance Criteria}
Scenario: Parse valid story
  Given a story with valid format
  When the parser processes the story
  Then the story is parsed successfully
  And the value statement is extracted
{panel}
```

---

## Panel Types

### 1. Value Statement Panel

**Title Matching**: Case-insensitive "value statement"
- ✅ `{panel:title=Value Statement}`
- ✅ `{panel:title=VALUE STATEMENT}`
- ✅ `{panel:title=value statement}`

**Content Format**: User story template

```
As a <PERSONA>, I want <GOAL>, so that <BENEFIT>
```

**Pattern Details**:
- `<PERSONA>`: Any text (no commas allowed)
- `I want` or `I want to`: Both forms accepted
- `<GOAL>`: Any text (up to "so that")
- `so that`: Required separator
- `<BENEFIT>`: Any text (to end of panel)

**Examples**:

✅ **Valid**:
```
As a user, I want to login, so that I can access my account
As a developer, I want clear error messages, so that I can debug faster
As a product manager, I want to see analytics, so that I can make data-driven decisions
As a system admin, I want to configure permissions, so that I can control access
```

❌ **Invalid**:
```
As a user, admin, I want...  // Comma in persona
I want to login  // Missing "As a"
As a user I want to login so that I can access  // Missing commas
As a user, I need to login, so that I can access  // "need" instead of "want"
```

**Extraction**:
```java
ValueStatement vs = story.getValueStatement();
System.out.println(vs.getPersona());  // "user"
System.out.println(vs.getGoal());     // "login"
System.out.println(vs.getBenefit());  // "I can access my account"
```

---

### 2. Requirements Panel

**Title Matching**: Case-insensitive "requirements"
- ✅ `{panel:title=Requirements}`
- ✅ `{panel:title=REQUIREMENTS}`
- ✅ `{panel:title=System Requirements}`

**Content Format**: Numbered list

```
1. First requirement
2. Second requirement
3. Third requirement
```

**Numbering Rules**:
- Must start with digit(s) followed by period and space: `\d+\. `
- Numbers don't need to be sequential (1, 2, 5 is valid)
- Multi-line requirements are supported (continuation lines don't start with number)

**Examples**:

✅ **Valid**:
```
1. The system must process orders
2. The "OrderService" service handles order creation
3. Orders are validated before processing
```

✅ **Multi-line**:
```
1. The system must process orders with the following steps:
   - Validate order data
   - Check inventory
   - Calculate total
2. The "PaymentService" service processes payments
```

✅ **Non-sequential**:
```
1. First requirement
3. Third requirement
10. Tenth requirement
```

❌ **Invalid**:
```
- First requirement  // Bullet, not numbered
1 First requirement  // Missing period after number
1.First requirement  // Missing space after period
a. First requirement // Letter, not number
```

**Extraction**:
```java
for (Requirement req : story.getRequirements()) {
    System.out.println(req.getNumber() + ": " + req.getText());
}
```

---

### 3. Acceptance Criteria Panel

**Title Matching**: Case-insensitive "acceptance criteria"
- ✅ `{panel:title=Acceptance Criteria}`
- ✅ `{panel:title=ACCEPTANCE CRITERIA}`
- ✅ `{panel:title=Acceptance criteria}`

**Content Format**: Gherkin-style scenarios

```
Scenario: <NAME>
  Given <precondition>
  When <action>
  Then <expected result>
```

**Keywords**:
- `Scenario:` - Starts a new scenario (required)
- `Given` - Preconditions (optional, multiple allowed)
- `When` - Actions (optional, multiple allowed)
- `Then` - Assertions (optional, multiple allowed)
- `And` - Continuation of previous statement type

**And Statement Rules**:
- `And` appends to the most recently used statement type
- Order matters: Given → When → Then
- `And` after `Then` creates another `Then` statement
- `And` after `When` creates another `When` statement

**Examples**:

✅ **Basic Scenario**:
```
Scenario: User login
  Given the user is on the login page
  When the user enters valid credentials
  Then the user is logged in successfully
```

✅ **Multiple Statements**:
```
Scenario: Order processing
  Given an order with 3 items
  And the user has sufficient balance
  When the order is submitted
  And payment is processed
  Then the order is confirmed
  And the user receives a confirmation email
```

✅ **Multiple Scenarios**:
```
Scenario: Valid login
  Given a registered user
  When they login with correct password
  Then they access their dashboard

Scenario: Invalid login
  Given a registered user
  When they login with wrong password
  Then they see an error message
```

❌ **Invalid**:
```
User login  // Missing "Scenario:"
Scenario  // Missing colon
Given user is logged in
When order is placed  // Different scenario, needs "Scenario:" first
```

**Extraction**:
```java
for (AcceptanceCriterion ac : story.getAcceptanceCriteria()) {
    System.out.println("Scenario: " + ac.getScenarioName());
    ac.getGivenStatements().forEach(g -> System.out.println("  Given " + g));
    ac.getWhenStatements().forEach(w -> System.out.println("  When " + w));
    ac.getThenStatements().forEach(t -> System.out.println("  Then " + t));
}
```

---

## Metadata Extraction

### Service Names

**Pattern**: `"ServiceName" service` (case-insensitive)

**Regex**: `"([^"]+)" service`

**Examples**:
```
The "OrderService" service processes orders
→ Extracts: ["OrderService"]

The "Order Service" service and "Payment Service" service
→ Extracts: ["Order Service", "Payment Service"]

the "order service" SERVICE handles orders
→ Extracts: ["order service"]  // Preserves original casing
```

**Usage**:
```java
Requirement req = new Requirement(1,
    "The \"OrderService\" service processes orders");
List<String> services = req.getServices();  // ["OrderService"]
```

### Event Names

**Pattern**: `"EventName" event`

**Regex**: `"(\w+)" event`

**Constraint**: Event name must be a single word (alphanumeric + underscore)

**Examples**:
```
Publishes "OrderCreated" event
→ Extracts: ["OrderCreated"]

The "OrderCreated" event and "OrderUpdated" event
→ Extracts: ["OrderCreated", "OrderUpdated"]

Publishes "Order Created" event
→ Extracts: []  // Space not allowed in event name
```

**Usage**:
```java
Requirement req = new Requirement(1,
    "Publishes \"OrderCreated\" event");
List<String> events = req.getEvents();  // ["OrderCreated"]
```

### Schema Names

**Pattern**: `"EventName" event` when text contains "avro schema"

**Regex**: `"(\w+)" event` (same as events)

**Format**: Appends `.avsc` extension

**Examples**:
```
Uses Avro schema with "OrderCreated" event
→ Extracts: ["OrderCreated.avsc"]

Avro schema for "OrderCreated" event and "OrderUpdated" event
→ Extracts: ["OrderCreated.avsc", "OrderUpdated.avsc"]

Uses "OrderCreated" event
→ Extracts: []  // No "avro schema" mention
```

**Usage**:
```java
Requirement req = new Requirement(1,
    "Uses Avro schema with \"OrderCreated\" event");
List<String> schemas = req.getSchemas();  // ["OrderCreated.avsc"]
```

---

## Parsing Rules

### Whitespace Handling

**Trimming**:
- Panel titles are trimmed
- Panel content is trimmed
- Individual lines are trimmed
- Requirement text is trimmed

**Empty Lines**:
- Empty lines in requirements are skipped
- Empty lines in acceptance criteria are skipped
- Leading/trailing empty lines in panels are removed

**Examples**:
```
{panel:title=  Value Statement  }
  As a user, I want to login, so that I can access my account
{panel}
```
Parsed as:
- Title: `"Value Statement"` (trimmed)
- Persona: `"user"` (trimmed)

### Case Sensitivity

**Case-Insensitive**:
- Panel titles: "Value Statement", "value statement", "VALUE STATEMENT"
- Service pattern: "service", "Service", "SERVICE"
- Value statement keywords: "As a", "as a", "AS A"

**Case-Sensitive**:
- Service names: `"OrderService"` ≠ `"orderservice"`
- Event names: `"OrderCreated"` ≠ `"ordercreated"`
- Gherkin keywords: Must be "Given", "When", "Then", "And", "Scenario:" (not "given", "WHEN")

### Multi-line Support

**Requirements**:
```
1. The system must process orders with these steps:
   Validate the order
   Check inventory
   Calculate total
2. Next requirement
```
Parsed as:
- Requirement 1: "The system must process orders with these steps: Validate the order Check inventory Calculate total"
- Requirement 2: "Next requirement"

**Acceptance Criteria**:
Each Given/When/Then is a single line. Use multiple statements or "And":
```
Scenario: Example
  Given precondition 1
  And precondition 2
  When action
  Then result
```

---

## Error Handling

### Parse Errors

**Null/Empty Input**:
```java
parser.parse(null);
// Throws: IllegalArgumentException("Story text cannot be null or empty")

parser.parse("");
// Throws: IllegalArgumentException("Story text cannot be null or empty")
```

**Missing Value Statement**:
```java
String story = """
{panel:title=Requirements}
1. Some requirement
{panel}
""";
parser.parse(story);
// Throws: IllegalArgumentException("Story must contain a Value Statement")
```

**Invalid Value Statement Format**:
```java
String story = """
{panel:title=Value Statement}
This is not a valid user story
{panel}
""";
parser.parse(story);
// Throws: IllegalArgumentException("Invalid value statement format: This is not a valid user story")
```

### Valid But Empty

**No Requirements**:
```java
String story = """
{panel:title=Value Statement}
As a user, I want to login, so that I can access my account
{panel}
""";
JiraStory parsed = parser.parse(story);
System.out.println(parsed.getRequirements().isEmpty());  // true
```

**No Acceptance Criteria**:
```java
// Same as above
System.out.println(parsed.getAcceptanceCriteria().isEmpty());  // true
```

---

## Complete Examples

### Example 1: Minimal Story

**Input**:
```
{panel:title=Value Statement}
As a user, I want to login, so that I can access my account
{panel}
```

**Output**:
```java
JiraStory story = parser.parse(input);

story.getValueStatement().getPersona();  // "user"
story.getValueStatement().getGoal();     // "login"
story.getValueStatement().getBenefit();  // "I can access my account"
story.getRequirements().isEmpty();       // true
story.getAcceptanceCriteria().isEmpty(); // true
```

### Example 2: Complete Story

**Input**:
```
{panel:title=Value Statement}
As a developer, I want automated test generation, so that I can save time
{panel}

{panel:title=Requirements}
1. The "TestGenerator" service must parse Jira stories
2. The system should publish "StoryParsed" event with Avro schema
3. Generated tests should use Cucumber framework
{panel}

{panel:title=Acceptance Criteria}
Scenario: Parse valid story
  Given a story with valid format
  When the parser processes the story
  Then the story is parsed successfully

Scenario: Invalid story
  Given a malformed story
  When the parser processes the story
  Then an error is thrown
{panel}
```

**Output**:
```java
JiraStory story = parser.parse(input);

// Value Statement
story.getValueStatement().getPersona();   // "developer"
story.getValueStatement().getGoal();      // "automated test generation"
story.getValueStatement().getBenefit();   // "I can save time"

// Requirements
story.getRequirements().size();           // 3

Requirement req1 = story.getRequirements().get(0);
req1.getNumber();                         // 1
req1.getText();                           // "The \"TestGenerator\" service..."
req1.getServices();                       // ["TestGenerator"]
req1.getEvents();                         // []

Requirement req2 = story.getRequirements().get(1);
req2.getEvents();                         // ["StoryParsed"]
req2.getSchemas();                        // ["StoryParsed.avsc"]

// Acceptance Criteria
story.getAcceptanceCriteria().size();     // 2

AcceptanceCriterion ac1 = story.getAcceptanceCriteria().get(0);
ac1.getScenarioName();                    // "Parse valid story"
ac1.getGivenStatements();                 // ["a story with valid format"]
ac1.getWhenStatements();                  // ["the parser processes the story"]
ac1.getThenStatements();                  // ["the story is parsed successfully"]

AcceptanceCriterion ac2 = story.getAcceptanceCriteria().get(1);
ac2.getScenarioName();                    // "Invalid story"
```

### Example 3: Complex Requirements

**Input**:
```
{panel:title=Requirements}
1. The "Order Service" service must validate orders before processing
2. When validation passes, publish "OrderValidated" event
3. Use Avro schema for "OrderValidated" event and "OrderRejected" event
4. The "Payment Service" service subscribes to "OrderValidated" event
5. Multi-line requirement with details:
   - Validate customer info
   - Check inventory levels
   - Verify payment method
{panel}
```

**Output**:
```java
// Requirement 1
req1.getServices();  // ["Order Service"]
req1.getEvents();    // []

// Requirement 2
req2.getEvents();    // ["OrderValidated"]
req2.getSchemas();   // []  // No "avro schema" mention

// Requirement 3
req3.getEvents();    // ["OrderValidated", "OrderRejected"]
req3.getSchemas();   // ["OrderValidated.avsc", "OrderRejected.avsc"]

// Requirement 4
req4.getServices();  // ["Payment Service"]
req4.getEvents();    // ["OrderValidated"]

// Requirement 5 (multi-line)
req5.getText();      // "Multi-line requirement... Validate customer info Check inventory..."
```

---

## Tips and Best Practices

### Writing Parseable Stories

1. **Always include Value Statement** - It's required
2. **Use consistent numbering** - Start at 1, increment
3. **Quote service/event names** - Use double quotes
4. **One panel type per story** - Don't repeat panel types
5. **Use standard Gherkin** - Given/When/Then/And only

### Common Mistakes

❌ **Forgetting quotes**:
```
The OrderService service...
```
✅ **Correct**:
```
The "OrderService" service...
```

❌ **Wrong keywords**:
```
As a user, I need to login, so that I can access
```
✅ **Correct**:
```
As a user, I want to login, so that I can access
```

❌ **Missing commas**:
```
As a user I want to login so that I can access
```
✅ **Correct**:
```
As a user, I want to login, so that I can access
```

### Performance Tips

1. **Reuse parser instance** - It's lightweight
2. **Use static topology extraction** - Avoid allocations
3. **Cache parsed stories** - If parsing same story multiple times
4. **Batch process** - Use parallel streams for multiple stories

---

## Parser Limitations

### Known Limitations

1. **Single Value Statement** - Only one per story
2. **Event names must be single word** - No spaces allowed
3. **No nested panels** - Panels cannot contain panels
4. **Gherkin keywords are fixed** - Cannot customize (Given/When/Then only)
5. **No parameterized scenarios** - Scenario Outline not supported

### Workarounds

**Multi-word events**: Use camelCase or snake_case
```
"OrderCreated" instead of "Order Created"
"order_created" instead of "Order Created"
```

**Complex acceptance criteria**: Use multiple statements
```
Given precondition 1
And precondition 2
And precondition 3
```

**Multiple value statements**: Use only the primary one in the story

---

## Version Compatibility

**Current Version**: 1.0.0
**Parser Format**: Stable
**Breaking Changes**: None planned

Any future changes to the parser format will be:
- Backward compatible where possible
- Documented in migration guides
- Versioned according to semantic versioning

For version history and migration guides, see [Development Guide](DEVELOPMENT.md).
