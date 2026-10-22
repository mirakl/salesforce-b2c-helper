# Salesforce Playwright Tools

Used to automate setting values of Salesforce Commerce Cloud (SFCC).

To run the tests, aka the feature, you need to have the following installed:

- Java 21
- Maven 3.8.1

Then you can run the test with the following command after having replaced the placeholders with the actual values. The flows read their inputs from environment variables:
```bash
SFCC_AUTOMATED_TESTS_USERNAME=VALUE SFCC_AUTOMATED_TESTS_PASSWORD=VALUE2 SFCC_AUTOMATED_TESTS_SECRET_KEY=VALUE3 SFCC_BASE_URL=VALUE4 mvn clean test -Dtest=FillCommerceApiSettingsTest
```

A GitHub action set-feature-switches is available to run this.
You need to set up on your repository the following secrets:
- SFCC_AUTOMATED_TESTS_USERNAME
- SFCC_AUTOMATED_TESTS_PASSWORD
- SFCC_AUTOMATED_TESTS_SECRET_KEY

And be sure to provide the valid base URL in parameter, e.g.: zdbe-001.dx.commercecloud.salesforce.com
