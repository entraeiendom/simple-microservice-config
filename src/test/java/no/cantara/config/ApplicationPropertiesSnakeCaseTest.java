package no.cantara.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationPropertiesSnakeCaseTest {

    @Test
    public void thatEnvironmentVariableWithoutEscapingCanOverrideProperty() {
        /*
         * In order to verify that environment-variables work, modify the environment of the test-runner
         * by inserting the environment variable 'base_url' with a value of 'overload' (anything except 'underscore'
         * will work) then observe that the test fails which confirms that environment variable overrides are working.
         */
        ApplicationProperties applicationProperties = ApplicationProperties.builderWithDefaults()
                .property("base_url", "underscore")
                .enableEnvironmentVariablesWithoutEscaping()
                .build();

        assertEquals("underscore", applicationProperties.get("base_url"));
    }

    @Test
    public void thatEnvironmentVariablesDoWarnAboutNonOverrides() {
        /*
         * In order to verify that environment-variables are using the wrong override-mehtod, modify the environment of
         * the test-runner by inserting the environment variable 'base_url' with a value of 'overload' (anything except
         * 'underscore' will work) then observe that the test passes, but with a WARN logged from
         * StoreBasedApplicationProperties with a message that you should use 'BASE_u_URL' to override the property.
         */
        ApplicationProperties applicationProperties = ApplicationProperties.builderWithDefaults()
                .property("base_url", "underscore")
                .enableEnvironmentVariables()
                .build();

        assertEquals("underscore", applicationProperties.get("base_url"));
    }

    @Test
    public void thatEnvironmentVariablesCanOverridePropertyWithUnderscore() {
        /*
         * In order to verify that environment-variables work, modify the environment of the test-runner
         * by inserting the environment variable 'BASE_u_URL' with a value of 'overload' (anything except 'underscore'
         * will work) then observe that the test fails which confirms that environment variable overrides are working.
         */
        ApplicationProperties applicationProperties = ApplicationProperties.builderWithDefaults()
                .property("base_url", "underscore")
                .enableEnvironmentVariables()
                .build();

        assertEquals("underscore", applicationProperties.get("base_url"));
    }

    @Test
    public void thatEnvironmentVariablesCanOverridePropertyWithDot() {
        /*
         * In order to verify that environment-variables work, modify the environment of the test-runner
         * by inserting the environment variable 'BASE_URL' with a value of 'overload' (anything except 'dot'
         * will work) then observe that the test fails which confirms that environment variable overrides are working.
         */
        ApplicationProperties applicationProperties = ApplicationProperties.builderWithDefaults()
                .property("base.url", "dot")
                .enableEnvironmentVariables()
                .build();

        assertEquals("dot", applicationProperties.get("base.url"));
    }

    @Test
    public void thatEnvironmentVariablesCanOverridePropertyWithDash() {
        /*
         * In order to verify that environment-variables work, modify the environment of the test-runner
         * by inserting the environment variable 'BASE_d_URL' with a value of 'overload' (anything except 'dash'
         * will work) then observe that the test fails which confirms that environment variable overrides are working.
         */
        ApplicationProperties applicationProperties = ApplicationProperties.builderWithDefaults()
                .property("base-url", "dash")
                .enableEnvironmentVariables()
                .build();

        assertEquals("dash", applicationProperties.get("base-url"));
    }
}
