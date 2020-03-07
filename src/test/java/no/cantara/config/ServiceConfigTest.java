package no.cantara.config;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServiceConfigTest {

    @Test
    public void loadProperties() {
        Properties serviceProperties = ServiceConfig.loadTestProperties();
        assertNotNull(serviceProperties);
        assertEquals(2, serviceProperties.size());
        assertEquals("standard", serviceProperties.getProperty("STANDARD_VALUE"));
    }

    @Test
    public void getProperty() {
    }
}