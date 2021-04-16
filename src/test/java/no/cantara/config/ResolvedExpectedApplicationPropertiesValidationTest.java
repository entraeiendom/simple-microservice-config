package no.cantara.config;

import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class ResolvedExpectedApplicationPropertiesValidationTest {


    @Test(expected = IllegalStateException.class)
    public void shouldThrowExeptionWhenValidationWithoutExpectedProperties() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .withProperty("base.url", "http-value")
                .build();

        applicationProperties.validate();


    }

    @Test(expected = RuntimeException.class)
    public void shouldValidateExpectedProperties() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .withExpectedProperties(new ExpectedApplicationProperties())
                .build();

        applicationProperties.validate();
    }

    private Properties getProperties(String key, String value) {
        final Properties properties = new Properties();
        properties.setProperty(key, value);
        return properties;
    }
}