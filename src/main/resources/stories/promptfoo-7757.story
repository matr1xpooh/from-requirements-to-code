{panel:title=Value Statement | titleBGColor=#b9d9ed}
 As a CI/CD engineer running promptfoo evaluations in a restricted environment, I want to export evaluation results as a static HTML site, so that I can host results on GitHub Pages or an internal static server without requiring a live promptfoo server.
{panel}

{panel:title=Requirements | titleBGColor=#b9d9ed}
1. add a "promptfoo export --static" command (or equivalent flag) that generates a self-contained static HTML/JS/CSS bundle of the evaluation results UI
2. ensure the exported static site supports viewing runs and results without requiring a running server or live API
3. ensure the exported bundle is suitable for deployment to GitHub Pages or any static web host
{panel}

{panel:title=Acceptance Criteria | titleBGColor=#b9d9ed}
Scenario: Static export is generated after a CI evaluation run
Given a completed promptfoo evaluation in a CI environment
When the user runs the static export command
Then a self-contained static site bundle is produced in the output directory

Scenario: Static site displays evaluation results without a server
Given a static export bundle deployed to GitHub Pages
When a user opens the site in a browser
Then they can view evaluation runs and results without any server-side component

Scenario: Static export does not require internal security-restricted server approval
Given a team subject to strict internal server approval processes
When they use the static export feature
Then they can share evaluation results by hosting the static files without needing server approval
{panel}
