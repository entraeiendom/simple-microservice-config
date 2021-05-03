package no.cantara.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SystemPropertiesTest {

    @Test
    public void thatSystemPropertiesOverride() {
        System.setProperty("base.url", "http-value");
        ApplicationProperties applicationProperties = ApplicationProperties.builder()
                .property("base.url", "base-value")
                .enableSystemProperties()
                .build();
        assertEquals("http-value", applicationProperties.get("base.url"));
    }

    @Test
    public void thatSystemPropertiesCanBeOverridden() {
        System.setProperty("base.url", "http-value");
        ApplicationProperties applicationProperties = ApplicationProperties.builder()
                .enableSystemProperties()
                .property("base.url", "base-value")
                .build();
        assertEquals("base-value", applicationProperties.get("base.url"));
    }

    @Test
    public void thatSystemPropertiesWithPrefixOverride() {
        System.setProperty("junit-test.base.url", "http-value");
        ApplicationProperties applicationProperties = ApplicationProperties.builder()
                .property("base.url", "base-value")
                .enableSystemProperties("junit-test.")
                .build();
        assertEquals("http-value", applicationProperties.get("base.url"));
    }
}
