package no.cantara.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResolvedApplicationPropertiesTest {

    @Test
    public void shouldGetPropertiesWithSingleProperty() {
        final ResolvedApplicationProperties applicationProperties = ResolvedApplicationProperties.Builder.builder().withProperty("base.url", "http-value").build();
        assertThat(applicationProperties.get("base.url")).isEqualTo("http-value");
    }
}