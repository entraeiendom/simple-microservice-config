package no.cantara.config;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubMapTest {

    @Test
    public void shouldGetPropertiesWithSingleProperty() {
        ApplicationProperties instance = ApplicationProperties.builder()
                .values()
                .put("level1.l2.a", "A")
                .put("level1.l2.l3.b", "B")
                .put("level1.l2.c", "C")
                .end()
                .build();

        Map<String, String> l2 = instance.subMap("level1.l2.");
        assertEquals(3, l2.size());
        assertTrue(l2.containsKey("a"));
        assertTrue(l2.containsKey("l3.b"));
        assertTrue(l2.containsKey("c"));
    }
}