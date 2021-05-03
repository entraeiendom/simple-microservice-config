package no.cantara.config;

import no.cantara.config.testsupport.ApplicationPropertiesTestHelper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EnvironmentVariableTest {

    @Before
    public void setUp() throws Exception {
        ApplicationPropertiesTestHelper.resetApplicationProperties();
        Map<String,String> envs = new HashMap<>();
        envs.put("SNAKE_CASE_TEST", "Set in env");
        envs.put("ENVIRONMENT_VARIABLE", "Set in env");
        setEnv(envs);
        ApplicationProperties.builderWithDefaults()
//                .enableEnvironmentVariables()
                .buildAndSetStaticSingleton();

    }

    @Ignore
    @Test
    public void snakeCaseTest() {
        assertEquals("Set in env", System.getenv("SNAKE_CASE_TEST"));
        assertEquals("Set in env", ApplicationProperties.getInstance().get("snake.case.test"));
        assertEquals(null, ApplicationProperties.getInstance().get("SNAKE.CASE.TEST"));
        assertEquals(null, ApplicationProperties.getInstance().get("SNAKE_CASE_TEST"));
        //Actual
        //assertEquals("from application properties", ApplicationProperties.getInstance().get("SNAKE_CASE_TEST"));
    }

    @Test
    public void environmentTest() {
        assertEquals("Set in env", System.getenv("ENVIRONMENT_VARIABLE"));
        assertEquals("Set in env", ApplicationProperties.getInstance().get("environment.variable"));
        assertEquals(null, ApplicationProperties.getInstance().get("ENVIRONMENT.VARIABLE"));
        assertEquals(null, ApplicationProperties.getInstance().get("ENVIRONMENT_VARIABLE"));
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
            Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
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
