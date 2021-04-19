package no.cantara.config;

import org.junit.Test;

public class ApplicationPropertiesValidationTest {


    @Test(expected = IllegalStateException.class)
    public void shouldThrowExeptionWhenValidationWithoutExpectedProperties() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .setProperty("base.url", "http-value")
                .build();

        applicationProperties.validate();


    }

    @Test
    public void shouldValidateExpectedProperties() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .setProperty("base.url", "http-value")
                .withExpectedProperties(ApiExpectedApplicationProperties.class)
                .build();

        applicationProperties.validate();
    }

    @Test
    public void shouldAcceptNonExpectedProperties() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .setProperty("base.url", "http-value")
                .setProperty("second.base.url", "another-http-value")
                .withExpectedProperties(ApiExpectedApplicationProperties.class)
                .build();

        applicationProperties.validate();
    }

    @Test(expected = RuntimeException.class)
    public void exceptionOnMissingPropertyValue() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .withProperties(ServiceConfig.loadProperties("blank.properties"))
                .withExpectedProperties(MyExpectedApplicationProperties.class)
                .build();

        applicationProperties.validate();
    }

    @Test(expected = RuntimeException.class)
    public void exceptionOnMissingPropertyKey() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .withExpectedProperties(MyExpectedApplicationProperties.class)
                .build();

        applicationProperties.validate();
    }


    @Test
    public void doubleClassOfExpectedProperties() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .setProperty(MyExpectedApplicationProperties.POSTGRES_URL, "postgres-value")
                .setProperty(ApiExpectedApplicationProperties.BASE_URL, "http-value")
                .withExpectedProperties(MyExpectedApplicationProperties.class, ApiExpectedApplicationProperties.class)
                .build();

        applicationProperties.validate();
    }

    public class MyExpectedApplicationProperties {

        public static final String POSTGRES_URL = "postgres.url";

    }

    public interface ApiExpectedApplicationProperties  {

        public static final String BASE_URL = "base.url";

    }

}