package an.story.creditcard;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Cucumber test runner for executing BDD tests.
 *
 * This runner:
 * 1. Discovers feature files in src/test/resources/features
 * 2. Executes scenarios using step definitions in an.story.creditcard.steps
 * 3. Generates test reports
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "an.story.creditcard.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports.html")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @ignore")
public class CucumberTestRunner {
    // This class serves as entry point for Cucumber tests
    // All configuration is done through annotations
}
