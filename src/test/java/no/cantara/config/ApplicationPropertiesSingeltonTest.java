package no.cantara.config;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationPropertiesSingeltonTest {


    @Before
    public void resetSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field instance = ApplicationProperties.class.getDeclaredField("singleton");
        instance.setAccessible(true);
        instance.set(null, null);
    }



    @Test(expected = RuntimeException.class)
    public void shouldGetPropertiesWithSingleProperty() {
        ApplicationProperties.getInstance();
    }

    @Test
    public void isSameObject() {
        ApplicationProperties.Builder.builder().setProperty("a", "value").init();
        assertThat(ApplicationProperties.getInstance()).isSameAs(ApplicationProperties.getInstance());
    }

}