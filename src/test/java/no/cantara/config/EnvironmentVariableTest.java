package no.cantara.config;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EnvironmentVariableTest {

    @Test
    public void snakeCaseTest() throws Exception {
        Map<String, String> envs = new HashMap<>();
        envs.put("SNAKE_CASE_TEST", "Set in env");
        setEnv(envs);
        ApplicationProperties instance = ApplicationProperties.builder()
                .defaults()
                .build();

        assertEquals("Set in env", System.getenv("SNAKE_CASE_TEST"));
        assertEquals("Set in env", instance.get("snake.case.test"));
        assertEquals(null, instance.get("SNAKE.CASE.TEST"));
        assertEquals(null, instance.get("SNAKE_CASE_TEST"));
        System.out.printf("%s%n", instance.sourcesOf("snake.case.test"));
    }

    public static class ExpectedEnvVars {
        public static final String VAR1 = "expected.only";
    }

    @Test
    public void environmentTest() throws Exception {
        Map<String, String> envs = new LinkedHashMap<>();
        envs.put("ENVIRONMENT_VARIABLE", "Set in env");
        envs.put("EXPECTED_ONLY", "Set in env");
        setEnv(envs);
        ApplicationProperties instance = ApplicationProperties.builder()
                .expectedProperties(ExpectedEnvVars.class)
                .defaults()
                .build();

        assertEquals("Set in env", System.getenv("ENVIRONMENT_VARIABLE"));
        assertEquals("Set in env", System.getenv("EXPECTED_ONLY"));
        assertEquals("Set in env", instance.get("environment.variable"));
        assertEquals("Set in env", instance.get("expected.only"));
        assertEquals(null, instance.get("ENVIRONMENT.VARIABLE"));
        assertEquals(null, instance.get("ENVIRONMENT_VARIABLE"));
        System.out.printf("%s%n", instance.sourcesOf("environment.variable"));

        System.out.printf("%s%n", instance.debugAll(true));
    }

    @Test
    public void envVarCasingTest() throws Exception {
        {
            // without environment override set
            ApplicationProperties config = ApplicationProperties.builder()
                    .property("I.am.CamelCased", "1")
                    .property("I.AM.CamelCased", "2")
                    .property("i.am.CamelCASed", "3")
                    .enableEnvironmentVariables()
                    .build();
            String val1 = config.get("I.am.CamelCased");
            String val2 = config.get("I.AM.CamelCased");
            String val3 = config.get("i.am.CamelCASed");
            String val4 = config.get("I.AM.CAMELCASED");
            assertEquals("1", val1);
            assertEquals("2", val2);
            assertEquals("3", val3);
            assertNull(val4);
        }
        {
            // with environment override set
            Map<String, String> envs = new LinkedHashMap<>();
            envs.put("I_AM_CAMELCASED", "Set in env");
            setEnv(envs);
            assertEquals("Set in env", System.getenv("I_AM_CAMELCASED"));

            ApplicationProperties config = ApplicationProperties.builder()
                    .property("I.am.CamelCased", "1")
                    .property("I.AM.CamelCased", "2")
                    .property("i.am.CamelCASed", "3")
                    .enableEnvironmentVariables()
                    .build();
            String val1 = config.get("I.am.CamelCased");
            String val2 = config.get("I.AM.CamelCased");
            String val3 = config.get("i.am.CamelCASed");
            String val4 = config.get("I.AM.CAMELCASED");
            assertEquals("Set in env", val1);
            assertEquals("Set in env", val2);
            assertEquals("Set in env", val3);
            assertNull(val4);
        }
    }

    @Test
    public void environmentCamelCaseTest() throws Exception {
        Map<String, String> envs = new LinkedHashMap<>();
        envs.put("IAMCAMELCASED", "Set in env");
        setEnv(envs);
        assertEquals("Set in env", System.getenv("IAMCAMELCASED"));

        {
            // With lowecase property used - env-var is translated to all specified variants
            ApplicationProperties instance = ApplicationProperties.builder()
                    .defaults()
                    .build();

            assertEquals(null, instance.get("IAMCAMELCASED")); // not an override (due to case mismatch)
            assertEquals(null, instance.get("iamcamelcased")); // not an override (due to case mismatch)
            assertEquals("Set in env", instance.get("iAmCamelCased"));
            System.out.printf("%s%n", instance.sourcesOf("iAmCamelCased"));
        }

        {
            // With other casing specified - env-var is translated to all specified variants
            ApplicationProperties instance = ApplicationProperties.builder()
                    .defaults()
                    .build();

            assertEquals(null, instance.get("IAMCAMELCASED"));
            assertEquals(null, instance.get("iamcamelcased"));
            assertEquals("Set in env", instance.get("iAmCamelCased"));
            System.out.printf("%s%n", instance.sourcesOf("iAmCamelCased"));
        }
    }

    protected static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }
}
