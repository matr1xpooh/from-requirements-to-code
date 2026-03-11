{panel:title=Value Statement | titleBGColor=#b9d9ed}
 As a promptfoo user, I want template variables like {{myVar}} defined in defaultTest llm-rubric assertions to be resolved at evaluation time, so that I can write uniform rubrics across all test cases without duplicating them per test case.
{panel}

{panel:title=Requirements | titleBGColor=#b9d9ed}
1. update the assertion evaluation pipeline to resolve template variables in "llm-rubric" assertions that are defined inside "defaultTest"
2. ensure that per-test-case variables are available in the template context when evaluating "defaultTest" rubrics
3. validate that rubric strings using {{varName}} syntax receive the actual variable value and not the literal template string
{panel}

{panel:title=Acceptance Criteria | titleBGColor=#b9d9ed}
Scenario: llm-rubric in defaultTest resolves template variables correctly
Given a defaultTest configuration with an llm-rubric assertion containing {{myVar}}
When a test case defines myVar as "hello world" and is evaluated
Then the grading LLM receives "hello world" in place of {{myVar}} in the rubric

Scenario: inline llm-rubric continues to resolve variables
Given a test case with an inline llm-rubric assertion containing {{myVar}}
When the test case is evaluated
Then the grading LLM receives the resolved variable value as before
{panel}
