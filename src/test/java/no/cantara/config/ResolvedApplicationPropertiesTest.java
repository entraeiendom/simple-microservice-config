package no.cantara.config;

import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class ResolvedApplicationPropertiesTest {


    @Test
    public void shouldGetPropertiesWithSingleProperty() {
        final Properties properties = getProperties("base.url", "http-value");
        final ResolvedApplicationProperties applicationProperties = ResolvedApplicationProperties.Builder
                .builder()
                .withProperty("base.url", "http-value")
                .build();
        assertThat(applicationProperties.get("base.url")).isEqualTo("http-value");

        assertThat(applicationProperties.getProperties()).isEqualTo(properties);

    }

    @Test
    public void shouldGetPropertiesWithPropertiesSet() {
        final Properties properties = getProperties("first.key", "first.value");
        properties.setProperty("second.key", "second.value");
        final ResolvedApplicationProperties applicationProperties = ResolvedApplicationProperties.Builder
                .builder()
                .withProperties(properties)
                .build();
        assertThat(applicationProperties.get("first.key")).isEqualTo("first.value");
        assertThat(applicationProperties.get("second.key")).isEqualTo("second.value");

        assertThat(applicationProperties.getProperties()).isEqualTo(properties);
    }

    private Properties getProperties(String key, String value) {
        final Properties properties = new Properties();
        properties.setProperty(key, value);
        return properties;
    }
}