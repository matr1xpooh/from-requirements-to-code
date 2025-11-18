# From Requirements to Code

## Overview

This project parses Jira-style stories written with Atlassian `{panel}` blocks and
produces structured Java domain objects you can use for automation:

- `ValueStatement`: captures persona, goal, and benefit.
- `Requirement`: extracts numbered requirements and infers referenced services,
  events, and Avro schemas.
- `AcceptanceCriterion`: stores Gherkin-style Given/When/Then steps.
- `ServiceTopology`: aggregates the services, events, and schemas mentioned
  across all requirements.

The sample `Main` class demonstrates parsing a story string end-to-end and
printing the resulting objects. See `sample-jira.story` for an input example.

## Prerequisites

- Java 17+
- Apache Maven 3.9+

## Run the Demo Parser

The simplest way to exercise the parser is via the bundled demo:

```bash
mvn exec:java \
  -Dexec.mainClass="an.story.main.JiraStoryParserMain" \
  -Dexec.classpathScope=compile
```

To parse your own story, edit the `storyText` string in
`src/main/java/an/story/main/Main.java` or adapt the demo to read from a file.

## Run the Tests

Unit tests cover the parser and domain model behaviour. Run them with:

```bash
mvn test
```

For a clean build that compiles sources and executes tests:

```bash
mvn clean verify
```

## Notes

- The build is configured for Java 17 and uses JUnit 5 via Maven Surefire.
- If you integrate this code with Google’s Agent Developer Kit, follow the
  streaming quickstart instructions in the ADK documentation for wiring agents
  into a web UI. The parser logic in this project is independent of that setup.