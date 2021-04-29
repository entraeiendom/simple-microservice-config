package no.cantara.config;

import no.cantara.config.testsupport.ApplicationPropertiesTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static no.cantara.config.ApplicationPropertiesSnakeCaseTest.ApiExpectedApplicationProperties.BASE_URL;
import static no.cantara.config.ApplicationPropertiesSnakeCaseTest.ApiExpectedApplicationProperties.SNAKE_CASE_TEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ApplicationPropertiesSnakeCaseTest {


    @Before
    public void setUp() throws Exception {
        System.setProperty("base_url","http-value");
        ApplicationPropertiesTestHelper.resetApplicationProperties();
        ApplicationProperties.builderWithDefaults()
                .property("JAVA_HOME", "/usr/lib/jvm/java-14-openjdk-amd64")
                .enableSystemProperties()
                .enableEnvironmentVariables("")
                .expectedProperties(ApiExpectedApplicationProperties.class)
                .buildAndSetStaticSingleton();
        
    }

    @Test
    public void snakeCaseEnvironment() {
        ApplicationProperties applicationProperties = ApplicationProperties.getInstance();

        assertEquals("http-value",applicationProperties.get(BASE_URL));
        assertEquals("http-value", applicationProperties.get("base_url"));
        assertEquals("from application properties", applicationProperties.get("SNAKE_CASE_TEST"));
        assertEquals("from application properties", applicationProperties.get(SNAKE_CASE_TEST));
    }

    public class ApiExpectedApplicationProperties {

        public static final String BASE_URL = "base.url";
        public static final String SNAKE_CASE_TEST = "SNAKE_CASE_TEST";

    }

    @After
    public void tearDown() throws Exception {
        ApplicationPropertiesTestHelper.resetApplicationProperties();
    }
}
