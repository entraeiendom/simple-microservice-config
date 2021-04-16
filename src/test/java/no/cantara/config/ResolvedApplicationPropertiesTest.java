package no.cantara.config;

import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class ResolvedApplicationPropertiesTest {

    @Test
    public void shouldGetPropertiesWithSingleProperty() {
        final ResolvedApplicationProperties applicationProperties = ResolvedApplicationProperties.Builder
                .builder()
                .withProperty("base.url", "http-value")
                .build();
        assertThat(applicationProperties.get("base.url")).isEqualTo("http-value");
    }

    @Test
    public void shouldGetPropertiesWithPropertiesSet() {
        final Properties properties = new Properties();
        properties.setProperty("first.key", "first.value");
        properties.setProperty("second.key", "second.value");
        final ResolvedApplicationProperties applicationProperties = ResolvedApplicationProperties.Builder
                .builder()
                .withProperties(properties)
                .build();
        assertThat(applicationProperties.get("first.key")).isEqualTo("first.value");
        assertThat(applicationProperties.get("second.key")).isEqualTo("second.value");
    }
}