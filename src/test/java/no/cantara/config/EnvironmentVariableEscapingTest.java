package no.cantara.config;

import org.junit.Test;

import static no.cantara.config.EnvironmentVariableEscaping.escape;
import static no.cantara.config.EnvironmentVariableEscaping.unescape;
import static org.junit.Assert.assertEquals;

public class EnvironmentVariableEscapingTest {

    @Test
    public void thatEscapeAndUnescapeWorks() {
        assertEscapeAndUnescape("level1", "LEVEL1");
        assertEscapeAndUnescape("level1.level2", "LEVEL1_LEVEL2");
        assertEscapeAndUnescape("level1-level2", "LEVEL1_d_LEVEL2");
        assertEscapeAndUnescape("level1_level2", "LEVEL1_u_LEVEL2");
        assertEscapeAndUnescape("level1_.level2", "LEVEL1_u__LEVEL2");
        assertEscapeAndUnescape("level1._level2", "LEVEL1__u_LEVEL2");
        assertEscapeAndUnescape("level1-._level2", "LEVEL1_d___u_LEVEL2");
        assertEscapeAndUnescape("level1.-_level2", "LEVEL1__d__u_LEVEL2");
        assertEscapeAndUnescape("level1_.-level2", "LEVEL1_u___d_LEVEL2");
        assertEscapeAndUnescape("level1_level2_level3.level4", "LEVEL1_u_LEVEL2_u_LEVEL3_LEVEL4");
        assertEscapeAndUnescape("level1---level2", "LEVEL1_d__d__d_LEVEL2");
        assertEscapeAndUnescape("level1-__-_--level2", "LEVEL1_d__u__u__d__u__d__d_LEVEL2");
    }

    private void assertEscapeAndUnescape(String propKey, String envKey) {
        assertEquals(envKey, escape(propKey));
        assertEquals(propKey, unescape(envKey));
    }
}