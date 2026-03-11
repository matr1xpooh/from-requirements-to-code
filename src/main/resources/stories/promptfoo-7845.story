{panel:title=Value Statement | titleBGColor=#b9d9ed}
 As a promptfoo user, I want the _conversation variable detection to use precise matching instead of substring search, so that prompts containing words like "pre_conversation_context" are not incorrectly forced to run at concurrency=1.
{panel}

{panel:title=Requirements | titleBGColor=#b9d9ed}
1. replace the naive .includes('_conversation') substring check with a word-boundary regex or template AST parser to detect standalone {{ _conversation }} usage
2. ensure that only prompts actually referencing the {{ _conversation }} template variable trigger the concurrency=1 restriction
3. add unit tests covering prompts that contain "_conversation" as part of a larger word to confirm they are not falsely flagged
{panel}

{panel:title=Acceptance Criteria | titleBGColor=#b9d9ed}
Scenario: Prompt containing _conversation as a substring does not force concurrency=1
Given a prompt that contains the text "pre_conversation_context" but not the {{ _conversation }} variable
When the prompt is evaluated
Then concurrency is not restricted to 1

Scenario: Prompt using {{ _conversation }} variable correctly forces concurrency=1
Given a prompt that contains the standalone template variable {{ _conversation }}
When the prompt is evaluated
Then concurrency is restricted to 1
{panel}
