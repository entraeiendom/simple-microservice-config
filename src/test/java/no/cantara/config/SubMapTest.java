package no.cantara.config;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SubMapTest {

    @Test
    public void thatSubMapGetCorrectProperties() {
        ApplicationProperties instance = ApplicationProperties.builder()
                .values()
                .put("level1.l2.a", "A")
                .put("level1.l2.l3.b", "123")
                .put("level1.l2.c", "C")
                .end()
                .build();

        Map<String, String> l2 = instance.subMap("level1.l2.");
        assertEquals(3, l2.size());
        assertEquals("A", l2.get("a"));
        assertEquals("123", l2.get("l3.b"));
        assertEquals("C", l2.get("c"));
    }

    @Test
    public void thatSubTreeGetCorrectProperties() {
        ApplicationProperties instance = ApplicationProperties.builder()
                .values()
                .put("level1.l1.34", "begin")
                .put("level1.l2.a", "A")
                .put("level1.l2.l3.b", "123")
                .put("level1.l2.c", "C")
                .put("level1.l2a.x", "end")
                .put("levelX.l1.34", "begin")
                .put("levelX.l2.a", "A")
                .put("levelX.l2.l3.b", "123")
                .put("levelX.l2.c", "C")
                .put("levelX.l2a.x", "end")
                .end()
                .build();

        ApplicationProperties l2 = instance.subTree("level1.l2.");
        assertEquals(3, l2.map().size());
        assertEquals("A", l2.get("a"));
        assertEquals(123, l2.asInt("l3.b", 1234));
        assertEquals("C", l2.get("c"));
    }
}