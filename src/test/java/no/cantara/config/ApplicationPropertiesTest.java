package no.cantara.config;

import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationPropertiesTest {


    @Test
    public void shouldGetPropertiesWithSingleProperty() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .setProperty("base.url", "http-value")
                .build();
        assertThat(applicationProperties.get("base.url")).isEqualTo("http-value");

        assertThat(applicationProperties.getMap()).containsEntry("base.url", "http-value");

    }

    @Test
    public void shouldGetPropertiesWithPropertiesSet() {
        final Properties properties = getProperties("first.key", "first.value");
        properties.setProperty("second.key", "second.value");
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .withProperties(properties)
                .build();
        assertThat(applicationProperties.get("first.key")).isEqualTo("first.value");
        assertThat(applicationProperties.get("second.key")).isEqualTo("second.value");

        assertThat(applicationProperties.getMap())
                .containsEntry("first.key", "first.value")
                .containsEntry("second.key", "second.value")
                .hasSize(2);
    }

    @Test
    public void orderMatters_withPropertiesOverridesAll() {
        final Properties properties = getProperties("first.key", "first.value");
        properties.setProperty("second.key", "second.value");
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .setProperty("first.property", "first.value")
                .withProperties(properties)
                .build();
        assertThat(applicationProperties.get("first.key")).isEqualTo("first.value");
        assertThat(applicationProperties.get("second.key")).isEqualTo("second.value");
        assertThat(applicationProperties.get("first.property")).isNull();

        assertThat(applicationProperties.getMap())
                .containsEntry("first.key", "first.value")
                .containsEntry("second.key", "second.value")
                .hasSize(2);
    }

    @Test
    public void orderMatters_withPropertyAdds() {
        final Properties properties = getProperties("first.key", "first.value");
        properties.setProperty("second.key", "second.value");
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .withProperties(properties)
                .setProperty("last.property", "last.value")
                .build();
        assertThat(applicationProperties.get("first.key")).isEqualTo("first.value");
        assertThat(applicationProperties.get("second.key")).isEqualTo("second.value");
        assertThat(applicationProperties.get("last.property")).isEqualTo("last.value");

        assertThat(applicationProperties.getMap())
                .containsEntry("first.key", "first.value")
                .containsEntry("second.key", "second.value")
                .containsEntry("last.property", "last.value")
                .hasSize(3);
    }

    @Test
    public void orderMatters_withPropertyCanOverride() {
        final Properties properties = getProperties("first.key", "first.value");
        properties.setProperty("second.key", "second.value");
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .withProperties(properties)
                .setProperty("second.key", "different.value")
                .build();
        assertThat(applicationProperties.get("first.key")).isEqualTo("first.value");
        assertThat(applicationProperties.get("second.key")).isEqualTo("different.value");

        final Properties resolved = new Properties(properties);
        resolved.setProperty("first.key", "first.value");
        resolved.setProperty("second.key", "different.value");
        assertThat(applicationProperties.getMap())
                .containsEntry("first.key", "first.value")
                .containsEntry("second.key", "different.value")
                .hasSize(2);
    }

    private Properties getProperties(String key, String value) {
        final Properties properties = new Properties();
        properties.setProperty(key, value);
        return properties;
    }
}