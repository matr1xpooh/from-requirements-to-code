# Development Guide

## Overview

This guide covers everything you need to know about developing, building, testing, and contributing to the From Requirements to Code project.

## Prerequisites

### Required
- **Java 17 or higher** - Download from [OpenJDK](https://openjdk.org/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
- **Apache Maven 3.9+** - Download from [Maven](https://maven.apache.org/download.cgi)

### Recommended
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions
- **Git**: For version control

### Verification

```bash
# Check Java version
java -version
# Expected: java version "17.x.x" or higher

# Check Maven version
mvn -version
# Expected: Apache Maven 3.9.x or higher
```

---

## Project Structure

```
from-requirements-to-code/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── an/story/
│   │   │       ├── parser/              # Story parser
│   │   │       │   └── JiraStoryParser.java
│   │   │       ├── domain_model/        # Domain objects
│   │   │       │   ├── JiraStory.java
│   │   │       │   ├── ValueStatement.java
│   │   │       │   ├── Requirement.java
│   │   │       │   ├── AcceptanceCriterion.java
│   │   │       │   ├── ServiceTopology.java
│   │   │       │   └── Panel.java
│   │   │       ├── gherkin_generator/   # Test generators
│   │   │       │   ├── command/         # Command implementations
│   │   │       │   ├── model/           # Generator models
│   │   │       │   ├── GherkinTestGenerator.java
│   │   │       │   ├── MultiServiceTestHarness.java
│   │   │       │   ├── TestContext.java
│   │   │       │   ├── EventBusSpy.java
│   │   │       │   ├── ServiceClient.java
│   │   │       │   └── SchemaRegistryClient.java
│   │   │       ├── creditcard/          # Sample application
│   │   │       │   ├── config/
│   │   │       │   ├── consumer/
│   │   │       │   ├── model/
│   │   │       │   ├── service/
│   │   │       │   └── main/
│   │   │       └── main/                # Entry points
│   │   │           ├── JiraStoryParserMain.java
│   │   │           └── TestGeneratorMain.java
│   │   └── resources/
│   │       └── avro/                    # Avro schemas
│   └── test/
│       ├── java/
│       │   └── an/story/
│       │       ├── parser/              # Parser tests
│       │       ├── domain_model/        # Domain tests
│       │       └── gherkin_generator/   # Generator tests
│       └── resources/
│           └── test-stories/            # Test fixtures
├── doc/                                 # Documentation
│   ├── README.md
│   ├── ARCHITECTURE.md
│   ├── API.md
│   ├── PARSER_REFERENCE.md
│   ├── GHERKIN_GENERATOR.md
│   ├── PERFORMANCE.md
│   └── DEVELOPMENT.md (this file)
├── target/                              # Build output (gitignored)
├── pom.xml                              # Maven configuration
├── Readme.MD                            # Project README
└── .gitignore
```

---

## Building the Project

### Clean Build

```bash
# Clean and compile
mvn clean compile

# Clean, compile, and run tests
mvn clean test

# Clean, compile, test, and package
mvn clean package

# Full build with verification
mvn clean verify
```

### Skip Tests

```bash
# Build without running tests
mvn clean package -DskipTests

# Compile without tests
mvn clean compile -DskipTests
```

### Install to Local Repository

```bash
# Install to ~/.m2/repository
mvn clean install
```

---

## Running the Application

### Parse a Jira Story

```bash
mvn exec:java \
  -Dexec.mainClass="an.story.main.JiraStoryParserMain" \
  -Dexec.classpathScope=compile
```

This runs the demo parser with a sample story.

### Generate Tests from Story

```bash
mvn exec:java \
  -Dexec.mainClass="an.story.main.TestGeneratorMain" \
  -Dexec.classpathScope=compile
```

This generates feature files and step definitions from a sample story.

### Run Credit Card Sample Application

```bash
mvn exec:java \
  -Dexec.mainClass="an.story.creditcard.main.CreditCardApplicationMain" \
  -Dexec.classpathScope=compile
```

---

## Testing

### Test Structure

```
src/test/java/an/story/
├── parser/
│   ├── CompleteStoryTest.java           # Full story parsing
│   ├── ValueStatementTests.java         # Value statement parsing
│   ├── RequirementTest.java             # Requirement parsing
│   ├── AcceptanceCriteriaTests.java     # AC parsing
│   └── ErrorHandlingTests.java          # Error cases
├── domain_model/
│   └── TopologyTest.java                # Topology extraction
└── gherkin_generator/
    ├── FeatureGenerationTest.java       # Feature file generation
    ├── StepDefinitionGenerationTest.java # Step definition generation
    └── TestHarnessTest.java             # Test harness
```

### Run All Tests

```bash
mvn test
```

**Expected Output**:
```
Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Run Specific Test Class

```bash
# Run single test class
mvn test -Dtest=CompleteStoryTest

# Run multiple test classes
mvn test -Dtest=ValueStatementTests,RequirementTest

# Run specific test method
mvn test -Dtest=CompleteStoryTest#testParseCompleteStory
```

### Run Tests by Pattern

```bash
# Run all tests matching pattern
mvn test -Dtest="*Test"

# Run all parser tests
mvn test -Dtest="an.story.parser.*Test"

# Run all generator tests
mvn test -Dtest="an.story.gherkin_generator.*Test"
```

### Test with Coverage

```bash
# Generate coverage report (requires Jacoco plugin)
mvn clean test jacoco:report

# View report at: target/site/jacoco/index.html
```

### Integration Tests

Integration tests use embedded Kafka (no Docker required):

```bash
# Run integration tests
mvn verify

# Run only integration tests
mvn test -Dtest="*IT"
```

---

## IDE Setup

### IntelliJ IDEA

1. **Import Project**:
   - File → Open → Select project directory
   - IntelliJ will auto-detect Maven project

2. **Set JDK**:
   - File → Project Structure → Project SDK → Select Java 17+

3. **Enable Annotation Processing**:
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"

4. **Run Tests**:
   - Right-click test class → Run
   - Or use Run menu → Run Tests

5. **Run Main Class**:
   - Right-click main class → Run
   - Or create Run Configuration

### Eclipse

1. **Import Project**:
   - File → Import → Maven → Existing Maven Projects
   - Select project directory

2. **Set JDK**:
   - Project → Properties → Java Build Path → JRE System Library → Java 17+

3. **Run Tests**:
   - Right-click test class → Run As → JUnit Test

4. **Run Main Class**:
   - Right-click main class → Run As → Java Application

### VS Code

1. **Install Extensions**:
   - Java Extension Pack
   - Maven for Java

2. **Open Project**:
   - File → Open Folder → Select project directory

3. **Run Tests**:
   - Test Explorer (left sidebar)
   - Click Run button next to test

4. **Run Main Class**:
   - Open main class
   - Click "Run" above main method

---

## Code Style

### Java Conventions

Follow standard Java conventions:

- **Class names**: PascalCase (`JiraStoryParser`)
- **Method names**: camelCase (`parseValueStatement()`)
- **Constants**: UPPER_SNAKE_CASE (`VALUE_STATEMENT_PATTERN`)
- **Variables**: camelCase (`storyText`)
- **Packages**: lowercase (`an.story.parser`)

### Formatting

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 100 characters (guideline, not strict)
- **Braces**: K&R style (opening brace on same line)

**Example**:
```java
public class Example {
    private static final Pattern PATTERN = Pattern.compile("regex");

    public void method() {
        if (condition) {
            // Code
        }
    }
}
```

### Documentation

- **Public APIs**: Javadoc required
- **Complex logic**: Inline comments
- **TODOs**: Use `// TODO:` format

**Example**:
```java
/**
 * Parses a Jira story into structured domain objects
 *
 * @param storyText Story text with panel blocks
 * @return Parsed JiraStory object
 * @throws IllegalArgumentException if story text is invalid
 */
public JiraStory parse(String storyText) {
    // Implementation
}
```

---

## Adding New Features

### 1. Parser Enhancement

**Example**: Add support for a new panel type

```java
// 1. Add to JiraStoryParser.java
private List<Dependency> parseDependencies(String content) {
    // Parse logic
}

// 2. Update parse() method
if (title.contains("dependencies")) {
    dependencies = parseDependencies(panel.getContent());
}

// 3. Add to JiraStory domain model
public class JiraStory {
    private List<Dependency> dependencies;
    // Getters/setters
}

// 4. Write tests
@Test
public void testParseDependencies() {
    // Test implementation
}
```

### 2. Generator Enhancement

**Example**: Add custom generator

```java
// 1. Create command
public class GenerateCustomOutputCommand implements Command<String> {
    private JiraStory story;

    public GenerateCustomOutputCommand(JiraStory story) {
        this.story = story;
    }

    @Override
    public String execute() {
        // Generation logic
        return output;
    }
}

// 2. Use in pipeline
GenerateCustomOutputCommand cmd = new GenerateCustomOutputCommand(story);
String output = cmd.execute();

// 3. Write tests
@Test
public void testCustomOutput() {
    // Test implementation
}
```

### 3. Domain Model Extension

**Example**: Add metadata to Requirement

```java
// 1. Add field
public class Requirement {
    private List<String> databases;  // New field

    public Requirement(int number, String text) {
        this.databases = extractDatabases(text);  // Extract
    }

    private List<String> extractDatabases(String text) {
        // Extraction logic
    }
}

// 2. Write tests
@Test
public void testExtractDatabases() {
    // Test implementation
}
```

---

## Testing Guidelines

### Unit Test Best Practices

1. **Test one thing**: Each test should verify one behavior
2. **Use descriptive names**: `testParseValueStatementWithValidInput()`
3. **AAA pattern**: Arrange, Act, Assert
4. **Mock external dependencies**: Use embedded Kafka for tests
5. **Test edge cases**: Null, empty, invalid input

**Example**:
```java
@Test
public void testParseValueStatementWithValidInput() {
    // Arrange
    String input = "As a user, I want to login, so that I can access my account";

    // Act
    ValueStatement vs = parser.parseValueStatement(input);

    // Assert
    assertEquals("user", vs.getPersona());
    assertEquals("login", vs.getGoal());
    assertEquals("I can access my account", vs.getBenefit());
}

@Test
public void testParseValueStatementWithNull() {
    // Assert exception
    assertThrows(IllegalArgumentException.class, () -> {
        parser.parseValueStatement(null);
    });
}
```

### Integration Test Best Practices

1. **Use real components**: Embedded Kafka, not mocks
2. **Test end-to-end**: Full parsing → generation → verification
3. **Clean up resources**: Use @After for teardown
4. **Isolate tests**: Each test should be independent

**Example**:
```java
@Test
public void testEndToEndGeneration() throws Exception {
    // Parse story
    JiraStory story = parser.parse(sampleStory);

    // Generate tests
    GenerateCompleteTestPackageCommand cmd =
        new GenerateCompleteTestPackageCommand(story, "com.test", "target/test");
    TestPackage pkg = cmd.execute();

    // Verify files created
    assertTrue(Files.exists(Paths.get(pkg.getFeatureFilePath())));

    // Verify content
    String content = pkg.getFeatureFileContent();
    assertTrue(content.contains("Feature:"));
}
```

---

## Debugging

### Enable Debug Logging

Add to test or main class:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Example {
    private static final Logger logger = LoggerFactory.getLogger(Example.class);

    public void method() {
        logger.debug("Parsing story: {}", storyText);
        logger.info("Parsed {} requirements", requirements.size());
    }
}
```

### Common Issues

**Issue**: `ClassNotFoundException` when running tests

**Solution**: Run `mvn clean compile test`

---

**Issue**: Embedded Kafka fails to start

**Solution**: Check port availability (default: 9092)
```bash
lsof -i :9092
```

---

**Issue**: OutOfMemoryError in tests

**Solution**: Increase heap size
```bash
export MAVEN_OPTS="-Xmx2g"
mvn test
```

---

## Performance Optimization

### Profiling

Use Java Mission Control or VisualVM:

```bash
# Run with JFR
java -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=profile.jfr \
     -cp target/classes \
     an.story.main.JiraStoryParserMain

# Analyze with JMC
jmc profile.jfr
```

### Benchmarking

Create benchmark class:

```java
public class Benchmark {
    public static void main(String[] args) {
        JiraStoryParser parser = new JiraStoryParser();
        String story = loadStory();

        // Warmup
        for (int i = 0; i < 1000; i++) {
            parser.parse(story);
        }

        // Measure
        long start = System.nanoTime();
        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            parser.parse(story);
        }
        long end = System.nanoTime();

        double avgMicros = (end - start) / 1000.0 / iterations;
        System.out.printf("Avg: %.2f μs%n", avgMicros);
    }
}
```

See [Performance Guide](PERFORMANCE.md) for detailed optimization strategies.

---

## Contributing

### Contribution Workflow

1. **Fork the repository**
2. **Create feature branch**: `git checkout -b feature/my-feature`
3. **Make changes**: Write code + tests
4. **Run tests**: `mvn clean test`
5. **Commit changes**: `git commit -m "Add feature X"`
6. **Push to fork**: `git push origin feature/my-feature`
7. **Create Pull Request**: Submit PR with description

### Commit Message Guidelines

Follow conventional commits:

```
<type>: <description>

<optional body>
```

**Types**:
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation
- `test:` Tests
- `refactor:` Code refactoring
- `perf:` Performance improvement
- `chore:` Build/tooling changes

**Examples**:
```
feat: add support for Scenario Outline parsing

fix: handle multi-line requirements correctly

docs: update API reference with new methods

perf: optimize pattern compilation in parser
```

### Pull Request Guidelines

**PR Title**: Same format as commit messages

**PR Description** should include:
- What: What changes were made
- Why: Why the changes are needed
- How: How the changes work
- Tests: What tests were added/updated

**Example**:
```markdown
## What
Add support for parsing Scenario Outline with Examples tables

## Why
Users need to create parameterized tests, which requires Scenario Outline support

## How
- Extended AcceptanceCriterion to support Examples table
- Updated parseAcceptanceCriteria() to recognize Examples keyword
- Added new ScenarioOutline domain model class

## Tests
- Added ScenarioOutlineTest with 5 test cases
- Updated existing tests for backward compatibility
```

### Code Review Process

1. **Automated checks**: CI runs tests and linting
2. **Peer review**: At least one reviewer approves
3. **Address feedback**: Make requested changes
4. **Merge**: Squash and merge when approved

---

## Continuous Integration

### GitHub Actions (if configured)

```yaml
name: Java CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean verify
```

---

## Release Process

### Version Numbering

Follow semantic versioning: `MAJOR.MINOR.PATCH`

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Creating a Release

1. **Update version in pom.xml**:
   ```xml
   <version>1.1.0</version>
   ```

2. **Update CHANGELOG** (if exists)

3. **Commit and tag**:
   ```bash
   git commit -m "Release v1.1.0"
   git tag v1.1.0
   git push origin v1.1.0
   ```

4. **Build and deploy**:
   ```bash
   mvn clean deploy
   ```

---

## Troubleshooting Build Issues

### Maven Dependency Issues

```bash
# Clear local repository cache
rm -rf ~/.m2/repository/com/example/my-java-project

# Force update dependencies
mvn clean install -U
```

### Compilation Errors

```bash
# Clean and rebuild
mvn clean compile

# Check Java version
mvn -version
```

### Test Failures

```bash
# Run with verbose output
mvn test -X

# Run specific test
mvn test -Dtest=FailingTest

# Skip tests temporarily
mvn clean package -DskipTests
```

---

## Resources

### Documentation
- [Architecture Guide](ARCHITECTURE.md)
- [API Reference](API.md)
- [Parser Reference](PARSER_REFERENCE.md)
- [Test Generator Guide](GHERKIN_GENERATOR.md)
- [Performance Guide](PERFORMANCE.md)

### External Resources
- [Maven Documentation](https://maven.apache.org/guides/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Cucumber Documentation](https://cucumber.io/docs/cucumber/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)

### Support
- **Issues**: Report bugs and request features
- **Discussions**: Ask questions and share ideas
- **Wiki**: Additional examples and tutorials

---

## License

See project repository for license information.

## Changelog

### Version 1.0.0 (March 2026)
- ✅ Initial release
- ✅ Jira story parser with panel support
- ✅ Value statement, requirements, acceptance criteria parsing
- ✅ Service topology extraction
- ✅ Feature file generation
- ✅ Step definition generation
- ✅ Multi-service test harness
- ✅ Static pattern compilation optimization (3-5x performance improvement)
- ✅ 22 unit and integration tests
- ✅ Complete documentation suite

### Future Versions
See [Architecture Guide](ARCHITECTURE.md) for planned enhancements.
