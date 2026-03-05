# Architecture Guide

## System Architecture

This document describes the architecture and design principles of the From Requirements to Code framework.

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Input: Jira Story Text                   │
│           (Panels: Value Statement, Requirements, AC)        │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    JiraStoryParser                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Panel        │  │ Value        │  │ Requirements │      │
│  │ Extractor    │→ │ Statement    │→ │ Parser       │      │
│  │              │  │ Parser       │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                            │                                │
│                            ▼                                │
│                   ┌──────────────┐                          │
│                   │ Acceptance   │                          │
│                   │ Criteria     │                          │
│                   │ Parser       │                          │
│                   └──────────────┘                          │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   Domain Model Objects                      │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐    │
│  │ JiraStory  │  │ Requirement│  │ AcceptanceCriterion│    │
│  └────────────┘  └────────────┘  └────────────────────┘    │
│  ┌────────────┐  ┌────────────┐                            │
│  │ Value      │  │ Service    │                            │
│  │ Statement  │  │ Topology   │                            │
│  └────────────┘  └────────────┘                            │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              Gherkin Test Generator (Commands)              │
│  ┌──────────────────┐  ┌──────────────────────┐            │
│  │ Feature File     │  │ Step Definitions     │            │
│  │ Generator        │  │ Generator            │            │
│  └──────────────────┘  └──────────────────────┘            │
│  ┌──────────────────┐  ┌──────────────────────┐            │
│  │ Scenario         │  │ Background           │            │
│  │ Generator        │  │ Generator            │            │
│  └──────────────────┘  └──────────────────────┘            │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│          Output: Feature Files + Step Definitions           │
│               (Executable Cucumber Tests)                   │
└─────────────────────────────────────────────────────────────┘
```

## Component Details

### 1. Parser Layer

#### JiraStoryParser
**Responsibility**: Parse Jira story text into structured domain objects

**Key Methods**:
- `parse(String storyText)` → `JiraStory` - Main entry point
- `extractPanels(String)` → `List<Panel>` - Extract panel blocks
- `parseValueStatement(String)` → `ValueStatement` - Parse user story
- `parseRequirements(String)` → `List<Requirement>` - Parse requirements
- `parseAcceptanceCriteria(String)` → `List<AcceptanceCriterion>` - Parse AC
- `extractTopologyStatic(JiraStory)` → `ServiceTopology` - Extract topology

**Design Patterns**:
- **Static Pattern Compilation**: All regex patterns compiled once as `static final` fields
- **Matcher Reuse**: Matchers created fresh per invocation, patterns reused
- **Fail-Fast Validation**: Throws `IllegalArgumentException` for invalid input

**Performance Optimizations**:
- Static patterns: `PANEL_PATTERN`, `VALUE_STATEMENT_PATTERN`, `NUMBER_PATTERN`
- Zero pattern compilation during parsing (3-5x speedup)
- Static utility method for topology extraction (eliminates object allocation)

### 2. Domain Model Layer

#### JiraStory
**Responsibility**: Root aggregate representing a complete story

**Structure**:
```java
public class JiraStory {
    private ValueStatement valueStatement;    // Required
    private List<Requirement> requirements;   // Optional
    private List<AcceptanceCriterion> acceptanceCriteria; // Optional
}
```

**Invariants**:
- Must have a ValueStatement (enforced in parser)
- Requirements and acceptance criteria are optional but usually present

#### ValueStatement
**Responsibility**: Capture user story in "As a... I want... so that..." format

**Structure**:
```java
public class ValueStatement {
    private String persona;   // Who
    private String goal;      // What
    private String benefit;   // Why
}
```

#### Requirement
**Responsibility**: Individual requirement with metadata extraction

**Structure**:
```java
public class Requirement {
    private int number;
    private String text;
    private List<String> services;  // Extracted via SERVICE_PATTERN
    private List<String> events;    // Extracted via EVENT_PATTERN
    private List<String> schemas;   // Extracted via SCHEMA_PATTERN
}
```

**Metadata Extraction**:
- Services: `"ServiceName" service` (case-insensitive)
- Events: `"EventName" event`
- Schemas: `"EventName" event` + "avro schema" → `EventName.avsc`

**Performance**: Static pattern compilation for all extractors

#### AcceptanceCriterion
**Responsibility**: Store Gherkin-style Given/When/Then steps

**Structure**:
```java
public class AcceptanceCriterion {
    private String scenarioName;
    private List<String> givenStatements;
    private List<String> whenStatements;
    private List<String> thenStatements;
}
```

**Rules**:
- Scenario name is required
- "And" statements append to most recent statement type
- Order matters: Given → When → Then

#### ServiceTopology
**Responsibility**: Aggregate view of all services, events, and schemas

**Structure**:
```java
public class ServiceTopology {
    private List<String> services;
    private List<String> events;
    private List<String> schemas;
}
```

**Construction**: Via `JiraStoryParser.extractTopologyStatic(story)`

### 3. Test Generator Layer

#### Command Pattern
All generators implement the `Command<T>` interface:

```java
public interface Command<T> {
    T execute();
}
```

**Benefits**:
- Composable operations
- Testable in isolation
- Clear separation of concerns

#### Key Commands

**GenerateFeatureFileFromStoryCommand**
- Input: `JiraStory`
- Output: Complete `.feature` file content
- Includes: Feature header, background, scenarios

**GenerateStepDefinitionsCommand**
- Input: `JiraStory`, package name
- Output: Complete Java step definition class
- Includes: Setup/teardown hooks, step methods

**GenerateCompleteTestPackageCommand**
- Input: `JiraStory`, base package, output directory
- Output: `TestPackage` (feature + steps + file paths)
- Orchestrates: Feature generation + step generation + file writing

**Supporting Commands**:
- `CollectUniqueStepsCommand` - Deduplicate steps across scenarios
- `GenerateStepMethodCommand` - Generate single step method
- `GenerateScenarioCommand` - Generate single scenario
- `GenerateBackgroundCommand` - Generate background section
- `DetermineAnnotationCommand` - Map step to Cucumber annotation

### 4. Test Infrastructure Layer

#### MultiServiceTestHarness
**Responsibility**: Manage embedded Kafka and services for testing

**Capabilities**:
- Start/stop embedded Kafka broker
- Initialize services with Kafka config
- Capture events via EventBusSpy
- Schema validation

**Lifecycle**:
```java
@Before
public void setUp() {
    harness = new MultiServiceTestHarness(services, events);
    harness.setup();  // Start Kafka, init services
}

@After
public void tearDown() {
    harness.teardown();  // Stop services, Kafka
}
```

#### TestContext
**Responsibility**: Share state between step definitions

**Shared State**:
- Test harness reference
- Event spy for assertions
- Test data

## Design Principles

### 1. Separation of Concerns
- **Parser**: Text → Domain objects (no business logic)
- **Domain Model**: Pure data structures (no I/O)
- **Generators**: Domain objects → Code (no parsing)

### 2. Immutability Where Possible
- Value objects (ValueStatement, Panel) are effectively immutable
- Domain objects use defensive copying where needed

### 3. Fail-Fast Validation
- Parser validates structure immediately
- Throws clear exceptions for malformed input
- No partial/corrupt objects

### 4. Performance-First
- Static pattern compilation (zero runtime compilation)
- Static utility methods (no unnecessary allocations)
- Efficient string operations

### 5. Command Pattern for Generation
- Each generator is a discrete command
- Composable and testable
- Clear inputs and outputs

## Data Flow

### Parsing Flow
```
Jira Text
    → extractPanels() → List<Panel>
    → parseValueStatement() → ValueStatement
    → parseRequirements() → List<Requirement>
        → extractServices() → List<String>
        → extractEvents() → List<String>
        → extractSchemas() → List<String>
    → parseAcceptanceCriteria() → List<AcceptanceCriterion>
    → JiraStory (assembled)
```

### Generation Flow
```
JiraStory
    → extractTopologyStatic() → ServiceTopology
    → GenerateFeatureFileFromStoryCommand
        → GenerateFeatureNameCommand
        → GenerateBackgroundCommand
        → GenerateScenarioCommand (per criterion)
        → Feature File String
    → GenerateStepDefinitionsCommand
        → CollectUniqueStepsCommand
        → GenerateStepMethodCommand (per unique step)
        → GenerateStepDefinitionClassNameCommand
        → Step Definition Java Class
```

## Extension Points

### Custom Parsers
Extend parsing for additional panel types:

```java
public class CustomJiraStoryParser extends JiraStoryParser {
    @Override
    protected void handleCustomPanel(Panel panel) {
        // Custom panel processing
    }
}
```

### Custom Generators
Implement new generators using Command pattern:

```java
public class GenerateCustomOutputCommand implements Command<String> {
    private final JiraStory story;

    @Override
    public String execute() {
        // Custom generation logic
    }
}
```

### Custom Metadata Extraction
Extend Requirement to extract additional metadata:

```java
public class ExtendedRequirement extends Requirement {
    private List<String> customMetadata;

    // Add custom extraction patterns
}
```

## Performance Characteristics

| Operation | Time Complexity | Space Complexity | Notes |
|-----------|----------------|------------------|-------|
| parse() | O(n) | O(n) | n = input length |
| extractTopologyStatic() | O(m) | O(k) | m = requirements, k = unique services |
| Pattern compilation | O(1) | O(1) | Static, amortized to class load |
| Generate feature | O(s) | O(s) | s = number of scenarios |
| Generate steps | O(u) | O(u) | u = unique steps |

**Key Metrics** (typical story: 3 requirements, ~18 lines):
- Parse time: ~200μs (vs 800μs pre-optimization)
- Pattern compilations: 0 (vs 15 pre-optimization)
- Memory allocations: 60% less temporary objects

See [Performance Guide](PERFORMANCE.md) for detailed benchmarks.

## Testing Strategy

### Unit Tests
- Parser components tested in isolation
- Domain model validation
- Generator output verification

### Integration Tests
- End-to-end parsing → generation
- Multi-service test harness
- Embedded Kafka testing

### Test Coverage
- 22 test methods across 7 test classes
- Core parsing: 100% coverage
- Edge cases and error handling

## Dependencies

### Runtime Dependencies
- None (parser is self-contained)

### Test Dependencies
- JUnit 5.11.4
- Cucumber (for generated tests)
- Kafka 3.8.1 (for integration tests)
- Spring Kafka Test (embedded broker)
- Avro 1.12.1 (schema serialization)

### Build Dependencies
- Maven Compiler Plugin 3.14.0
- Maven Surefire Plugin 3.2.5
- Avro Maven Plugin 1.12.1

## Future Enhancements

### Potential Improvements
1. **Parser caching**: Cache parsed stories by content hash
2. **Incremental parsing**: Re-parse only changed panels
3. **Async generation**: Parallel test file generation
4. **Template engine**: Pluggable templates for generation
5. **Schema inference**: Auto-generate Avro schemas from requirements

### Backward Compatibility
All enhancements maintain backward compatibility via:
- Semantic versioning
- Deprecated annotations for old APIs
- Migration guides for breaking changes
