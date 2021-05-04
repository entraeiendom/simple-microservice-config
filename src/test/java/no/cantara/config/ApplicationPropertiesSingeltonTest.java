package no.cantara.config;

import no.cantara.config.testsupport.ApplicationPropertiesTestHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationPropertiesSingeltonTest {


    @BeforeClass
    public static void enableMutableSingleton() {
        ApplicationPropertiesTestHelper.enableMutableSingleton();
    }

    @Test(expected = IllegalStateException.class)
    public void thatNullInstanceWillThrowFailFastExceptionWhenAccessed() {
        ApplicationPropertiesTestHelper.clearSingleton();
        ApplicationProperties.getInstance();
    }

    @Test
    public void isSameObject() {
        ApplicationProperties.builder().property("a", "value").buildAndSetStaticSingleton();
        assertThat(ApplicationProperties.getInstance()).isSameAs(ApplicationProperties.getInstance());
    }

}