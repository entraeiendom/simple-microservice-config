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

    @Test
    public void orderMatters_withPropertiesOverridesAll() {
        final Properties properties = getProperties("first.key", "first.value");
        properties.setProperty("second.key", "second.value");
        final ResolvedApplicationProperties applicationProperties = ResolvedApplicationProperties.Builder
                .builder()
                .withProperty("first.property", "first.value")
                .withProperties(properties)
                .build();
        assertThat(applicationProperties.get("first.key")).isEqualTo("first.value");
        assertThat(applicationProperties.get("second.key")).isEqualTo("second.value");
        assertThat(applicationProperties.get("first.property")).isNull();

        assertThat(applicationProperties.getProperties()).isEqualTo(properties);
    }

    @Test
    public void orderMatters_withPropertyAdds() {
        final Properties properties = getProperties("first.key", "first.value");
        properties.setProperty("second.key", "second.value");
        final ResolvedApplicationProperties applicationProperties = ResolvedApplicationProperties.Builder
                .builder()
                .withProperties(properties)
                .withProperty("last.property", "last.value")
                .build();
        assertThat(applicationProperties.get("first.key")).isEqualTo("first.value");
        assertThat(applicationProperties.get("second.key")).isEqualTo("second.value");
        assertThat(applicationProperties.get("last.property")).isEqualTo("last.value");

        final Properties resolved = new Properties(properties);
        resolved.setProperty("first.key", "first.value");
        resolved.setProperty("second.key", "second.value");
        resolved.setProperty("last.property", "last.value");
        assertThat(applicationProperties.getProperties()).isEqualTo(resolved);
    }

    @Test
    public void orderMatters_withPropertyCanOverride() {
        final Properties properties = getProperties("first.key", "first.value");
        properties.setProperty("second.key", "second.value");
        final ResolvedApplicationProperties applicationProperties = ResolvedApplicationProperties.Builder
                .builder()
                .withProperties(properties)
                .withProperty("second.key", "different.value")
                .build();
        assertThat(applicationProperties.get("first.key")).isEqualTo("first.value");
        assertThat(applicationProperties.get("second.key")).isEqualTo("different.value");

        final Properties resolved = new Properties(properties);
        resolved.setProperty("first.key", "first.value");
        resolved.setProperty("second.key", "different.value");
        assertThat(applicationProperties.getProperties()).isEqualTo(resolved);
    }

    private Properties getProperties(String key, String value) {
        final Properties properties = new Properties();
        properties.setProperty(key, value);
        return properties;
    }
}