{panel:title=Value Statement | titleBGColor=#b9d9ed}
 As a promptfoo test suite maintainer, I want to reference external files for nested assertion properties such as "type" and "provider", so that I can define assertion configurations once and reuse them across multiple test cases following the DRY principle.
{panel}

{panel:title=Requirements | titleBGColor=#b9d9ed}
1. extend the external file reference resolution (file://) to support nested assertion properties including "type" and "provider" in addition to the top-level "value"
2. ensure that a file reference such as file://rubrics/warm-greeting.txt is resolved at evaluation time for any supported nested assertion property
3. document which assertion properties support external file references
{panel}

{panel:title=Acceptance Criteria | titleBGColor=#b9d9ed}
Scenario: External file reference resolves for nested "value" property
Given an assertion with value set to "file://rubrics/greeting.txt"
When the assertion is evaluated
Then the content of greeting.txt is used as the assertion value

Scenario: External file reference resolves for nested "provider" property
Given an assertion with provider set to "file://providers/custom-provider.yaml"
When the assertion is evaluated
Then the configuration in custom-provider.yaml is loaded as the assertion provider

Scenario: Unsupported property does not resolve file reference
Given an assertion property that does not support external file references
When a file:// reference is provided for that property
Then an informative error is returned indicating the property is not supported
{panel}
