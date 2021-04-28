package no.cantara.config.testsupport;

import no.cantara.config.ApplicationProperties;

import java.lang.reflect.Method;

/**
 * TODO: This class should be moved into a separate property-config-test library to avoid that applications use it in
 * TODO: non-test contexts.
 */
public class ApplicationPropertiesTestHelper {

    public static void resetApplicationProperties() {
        try {
            Class<?> repoClazz = Class.forName("no.cantara.config.ApplicationPropertiesRepo");
            Method method = repoClazz.getDeclaredMethod("initInstance", ApplicationProperties.class);
            method.setAccessible(true);
            method.invoke(repoClazz, new Object[]{null});
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
