{panel:title=Value Statement | titleBGColor=#b9d9ed}
 As a Chase Bank product owner, I want to ensure that users over age of 120 are not allowed to apply for a credit card, so that we 
 maintain data integrity and comply with realistic age expectations.
{panel}

{panel:title=Requirements | titleBGColor=#b9d9ed}
1. update the "data cleanse" service to validate the applicant's age and produce an error if the age is greater than 120 years
2. ensure that the "aoaApplicantDataCleansedErrored" event is triggered for applicants with an age greater than 120
3. update the avro schema for the "aoaApplicantDataCleansedErrored" event to include an error message specific to age validation failure
{panel}

{panel:title=Acceptance Criteria | titleBGColor=#b9d9ed}
Scenario: User age is valid
Given the applicant's age is less than or equal to 120 years
When the application is processed by the Data Cleanse service
Then the "aoaApplicantDataCleansed" event is produced
{panel}


