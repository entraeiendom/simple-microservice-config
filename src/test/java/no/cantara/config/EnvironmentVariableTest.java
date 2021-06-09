package no.cantara.config;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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
    public void environmentCamelCaseTest() throws Exception {
        Map<String, String> envs = new LinkedHashMap<>();
        envs.put("IAMCAMELCASED", "Set in env");
        setEnv(envs);
        assertEquals("Set in env", System.getenv("IAMCAMELCASED"));

        {
            // env-var is always translated to property with lowercase key unless directed otherwise
            ApplicationProperties instance = ApplicationProperties.builder()
                    .defaults()
                    .build();

            assertEquals(null, instance.get("IAMCAMELCASED"));
            assertEquals(null, instance.get("iamcamelcased")); // not an override (due to case mismatch)
            assertEquals("from application properties", instance.get("iAmCamelCased"));
            System.out.printf("%s%n", instance.sourcesOf("iAmCamelCased"));
        }

        {
            // With casing specified - env-var is translated only to the casing as specified
            ApplicationProperties instance = ApplicationProperties.builder()
                    .defaults()
                    .envVarCasing("iAmCamelCased")
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
