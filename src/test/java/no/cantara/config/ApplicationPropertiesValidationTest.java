package no.cantara.config;

import no.cantara.config.testsupport.ApplicationPropertiesTestHelper;
import org.junit.BeforeClass;
import org.junit.Test;


public class ApplicationPropertiesValidationTest {

    @BeforeClass
    public static void enableMutableSingleton() {
        ApplicationPropertiesTestHelper.enableMutableSingleton();
    }


    @Test
    public void shouldValidateExpectedProperties() {
        ApplicationProperties.builder()
                .property("base.url", "http-value")
                .expectedProperties(ApiExpectedApplicationProperties.class)
                .buildAndSetStaticSingleton();

    }

    @Test
    public void shouldAcceptNonExpectedProperties() {
        ApplicationProperties.builder()
                .property("base.url", "http-value")
                .property("second.base.url", "another-http-value")
                .expectedProperties(ApiExpectedApplicationProperties.class)
                .buildAndSetStaticSingleton();
    }

    @Test(expected = RuntimeException.class)
    public void exceptionOnMissingPropertyValue() {
        ApplicationProperties.builder()
                .classpathPropertiesFile("blank.properties")
                .expectedProperties(MyExpectedApplicationProperties.class)
                .buildAndSetStaticSingleton();

    }

    @Test(expected = RuntimeException.class)
    public void exceptionOnMissingPropertyKey() {
        ApplicationProperties.builder()
                .expectedProperties(MyExpectedApplicationProperties.class)
                .buildAndSetStaticSingleton();

    }


    @Test
    public void doubleClassOfExpectedProperties() {
        ApplicationProperties.builder()
                .property(MyExpectedApplicationProperties.POSTGRES_URL, "postgres-value")
                .property(ApiExpectedApplicationProperties.BASE_URL, "http-value")
                .expectedProperties(MyExpectedApplicationProperties.class, ApiExpectedApplicationProperties.class)
                .buildAndSetStaticSingleton();

    }

    public class MyExpectedApplicationProperties {

        public static final String POSTGRES_URL = "postgres.url";

    }

    public class ApiExpectedApplicationProperties {

        public static final String BASE_URL = "base.url";

    }

}