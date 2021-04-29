package no.cantara.config;

import no.cantara.config.testsupport.ApplicationPropertiesTestHelper;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationPropertiesSingeltonTest {


    @Before
    public void resetSingleton() {
        ApplicationPropertiesTestHelper.resetApplicationProperties();
    }



    @Test(expected = RuntimeException.class)
    public void shouldGetPropertiesWithSingleProperty() {
        ApplicationProperties.getInstance();
    }

    @Test
    public void isSameObject() {
        ApplicationProperties.builder().property("a", "value").buildAndSetStaticSingleton();
        assertThat(ApplicationProperties.getInstance()).isSameAs(ApplicationProperties.getInstance());
    }

}