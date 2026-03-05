# From Requirements to Code - Documentation

## Overview

**From Requirements to Code** is a sophisticated Java framework that transforms Jira-style stories into executable test code. It bridges the gap between business requirements and technical implementation by parsing structured requirements and generating complete Cucumber/Gherkin test suites.

## Key Features

- **Story Parsing**: Parses Jira stories with Atlassian `{panel}` blocks into structured domain objects
- **Topology Extraction**: Automatically identifies services, events, and schemas from requirements
- **Test Generation**: Creates complete Cucumber feature files and Java step definitions
- **Multi-Service Testing**: Built-in test harness for event-driven microservices
- **High Performance**: Optimized regex pattern compilation for fast parsing

## Project Components

### Core Modules

1. **Parser Module** (`an.story.parser`)
   - `JiraStoryParser` - Main parser for Jira story format
   - Extracts value statements, requirements, and acceptance criteria

2. **Domain Model** (`an.story.domain_model`)
   - `JiraStory` - Complete story representation
   - `ValueStatement` - User story format (persona, goal, benefit)
   - `Requirement` - Individual requirements with extracted metadata
   - `AcceptanceCriterion` - Gherkin-style acceptance criteria
   - `ServiceTopology` - Service/event/schema topology

3. **Test Generator** (`an.story.gherkin_generator`)
   - Command-based test generation
   - Feature file generation
   - Step definition generation
   - Multi-service test harness

4. **Sample Application** (`an.story.creditcard`)
   - Credit card application processing example
   - Kafka-based event-driven architecture
   - Demonstrates real-world usage

## Documentation Index

- **[Architecture Guide](ARCHITECTURE.md)** - System design and component architecture
- **[API Reference](API.md)** - Detailed API documentation and usage examples
- **[Parser Reference](PARSER_REFERENCE.md)** - Story format and parsing rules
- **[Test Generator Guide](GHERKIN_GENERATOR.md)** - Automated test generation
- **[Performance Guide](PERFORMANCE.md)** - Performance optimizations and benchmarks
- **[Development Guide](DEVELOPMENT.md)** - Building, testing, and contributing

## Quick Start

### Prerequisites

- Java 17+
- Apache Maven 3.9+

### Parse a Jira Story

```java
import an.story.parser.JiraStoryParser;
import an.story.domain_model.JiraStory;

JiraStoryParser parser = new JiraStoryParser();
JiraStory story = parser.parse(storyText);

System.out.println("Persona: " + story.getValueStatement().getPersona());
System.out.println("Requirements: " + story.getRequirements().size());
```

### Generate Test Code

```java
import an.story.gherkin_generator.command.*;

// Generate feature file
GenerateFeatureFileFromStoryCommand featureCmd =
    new GenerateFeatureFileFromStoryCommand(story);
String featureFile = featureCmd.execute();

// Generate step definitions
GenerateStepDefinitionsCommand stepCmd =
    new GenerateStepDefinitionsCommand(story, "com.example.steps");
String stepDefinitions = stepCmd.execute();
```

### Run Demo

```bash
# Parse a sample story
mvn exec:java -Dexec.mainClass="an.story.main.JiraStoryParserMain"

# Generate tests from story
mvn exec:java -Dexec.mainClass="an.story.main.TestGeneratorMain"

# Run all tests
mvn test
```

## Use Cases

### 1. Automated Test Generation
Convert Jira stories directly into executable Cucumber tests, eliminating manual test writing.

### 2. Service Topology Discovery
Extract service dependencies, events, and schemas from requirements for architecture documentation.

### 3. BDD Workflow Automation
Streamline Behavior-Driven Development by auto-generating test scaffolding from acceptance criteria.

### 4. Microservices Testing
Use the built-in multi-service test harness for event-driven integration testing.

## Technology Stack

- **Language**: Java 17
- **Build**: Apache Maven
- **Testing**: JUnit 5, Cucumber
- **Event Streaming**: Apache Kafka 3.8.1
- **Serialization**: Apache Avro 1.12.1
- **Test Infrastructure**: Embedded Kafka (Spring Kafka Test)

## Performance

The parser is optimized for high-performance parsing:

- **Pattern Compilation**: Static regex patterns compiled once at class load
- **Memory Efficiency**: 40% reduction in allocations vs naive implementation
- **Parse Speed**: 3-5x faster than repeated pattern compilation
- **Scalability**: Suitable for batch processing and long-running services

See [Performance Guide](PERFORMANCE.md) for detailed benchmarks.

## Project Status

**Version**: 1.0.0
**Status**: Production Ready
**Last Updated**: March 2026

## License

See project repository for license information.

## Support

For issues, questions, or contributions, see the [Development Guide](DEVELOPMENT.md).
