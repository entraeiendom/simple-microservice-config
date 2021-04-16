package no.cantara.config;

import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class ResolvedApplicationPropertiesValidationTest {


    @Test(expected = IllegalStateException.class)
    public void shouldThrowExeptionWhenValidationWithoutExpectedProperties() {
        final ResolvedApplicationProperties applicationProperties = ResolvedApplicationProperties.Builder
                .builder()
                .withProperty("base.url", "http-value")
                .build();

        applicationProperties.validate();


    }

    @Test(expected = RuntimeException.class)
    public void shouldValidateExpectedProperties() {
        final ResolvedApplicationProperties applicationProperties = ResolvedApplicationProperties.Builder
                .builder()
                .withExpectedProperties(new ApplicationProperties())
                .build();

        applicationProperties.validate();
    }

    private Properties getProperties(String key, String value) {
        final Properties properties = new Properties();
        properties.setProperty(key, value);
        return properties;
    }
}