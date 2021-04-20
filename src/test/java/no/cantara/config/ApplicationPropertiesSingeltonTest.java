package no.cantara.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationPropertiesSingeltonTest {


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