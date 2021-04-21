package no.cantara.config;

import org.junit.Before;
import org.junit.Test;


public class ApplicationPropertiesValidationTest {

    @Before
    public void resetSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        ApplicationPropertiesTestHelper.resetApplicationProperties();
    }


    @Test(expected = IllegalStateException.class)
    public void shouldThrowExeptionWhenValidationWithoutExpectedProperties() {
        ApplicationProperties.Builder
                .builder()
                .setProperty("base.url", "http-value")
                .init();
        final ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
        applicationProperties.validate();


    }

    @Test
    public void shouldValidateExpectedProperties() {
        ApplicationProperties.Builder
                .builder()
                .setProperty("base.url", "http-value")
                .withExpectedProperties(ApiExpectedApplicationProperties.class)
                .init();

    }

    @Test
    public void shouldAcceptNonExpectedProperties() {
        ApplicationProperties.Builder
                .builder()
                .setProperty("base.url", "http-value")
                .setProperty("second.base.url", "another-http-value")
                .withExpectedProperties(ApiExpectedApplicationProperties.class)
                .init();
    }

    @Test(expected = RuntimeException.class)
    public void exceptionOnMissingPropertyValue() {
        ApplicationProperties.Builder
                .builder()
                .withProperties(ServiceConfig.loadProperties("blank.properties"))
                .withExpectedProperties(MyExpectedApplicationProperties.class)
                .init();

    }

    @Test(expected = RuntimeException.class)
    public void exceptionOnMissingPropertyKey() {
        ApplicationProperties.Builder
                .builder()
                .withExpectedProperties(MyExpectedApplicationProperties.class)
                .init();

    }


    @Test
    public void doubleClassOfExpectedProperties() {
        ApplicationProperties.Builder
                .builder()
                .setProperty(MyExpectedApplicationProperties.POSTGRES_URL, "postgres-value")
                .setProperty(ApiExpectedApplicationProperties.BASE_URL, "http-value")
                .withExpectedProperties(MyExpectedApplicationProperties.class, ApiExpectedApplicationProperties.class)
                .init();

    }

    public class MyExpectedApplicationProperties {

        public static final String POSTGRES_URL = "postgres.url";

    }

    public class ApiExpectedApplicationProperties  {

        public static final String BASE_URL = "base.url";

    }

}