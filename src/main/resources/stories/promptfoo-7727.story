{panel:title=Value Statement | titleBGColor=#b9d9ed}
 As a promptfoo user on a free-tier provider account, I want to configure a maximum retry count of zero for HTTP 429 rate limit responses, so that I can fail fast instead of retrying indefinitely and causing memory bloat when rate limits reset only after hours.
{panel}

{panel:title=Requirements | titleBGColor=#b9d9ed}
1. add a "maxRetries" configuration option at the provider level that controls the number of retry attempts on HTTP 429 responses
2. ensure that setting "maxRetries: 0" causes the provider to immediately fail the request without retrying on a 429
3. apply the "maxRetries" setting consistently across all built-in providers without requiring provider-specific workarounds
{panel}

{panel:title=Acceptance Criteria | titleBGColor=#b9d9ed}
Scenario: Provider with maxRetries=0 fails immediately on 429
Given a provider configured with maxRetries set to 0
When the provider receives an HTTP 429 response from the API
Then the request fails immediately without any retry attempts

Scenario: Provider with default maxRetries retries on 429
Given a provider with no maxRetries configuration (default behavior)
When the provider receives an HTTP 429 response
Then the provider retries the request according to the existing retry policy

Scenario: maxRetries is respected across all built-in providers
Given maxRetries set to 0 in the shared provider configuration
When any built-in provider encounters a 429
Then the fail-fast behavior applies without needing per-provider configuration
{panel}
