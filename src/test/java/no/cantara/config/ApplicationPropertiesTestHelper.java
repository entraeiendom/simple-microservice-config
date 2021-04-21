package no.cantara.config;

import java.lang.reflect.Field;

public class ApplicationPropertiesTestHelper {

    public static void resetApplicationProperties() throws NoSuchFieldException, IllegalAccessException {
        Field instance = ApplicationProperties.class.getDeclaredField("singleton");
        instance.setAccessible(true);
        instance.set(null, null);

    }
}
