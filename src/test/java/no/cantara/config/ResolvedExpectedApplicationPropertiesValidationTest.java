package no.cantara.config;

import org.junit.Test;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ResolvedExpectedApplicationPropertiesValidationTest {


    @Test(expected = IllegalStateException.class)
    public void shouldThrowExeptionWhenValidationWithoutExpectedProperties() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .withProperty("base.url", "http-value")
                .build();

        applicationProperties.validate();


    }

    @Test
    public void shouldValidateExpectedProperties() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .withProperty("base.url", "http-value")
                .withExpectedProperties(new MyExpectedApplicationProperties())
                .build();

        applicationProperties.validate();
    }

    @Test
    public void shouldAcceptNonExpectedProperties() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .withProperty("base.url", "http-value")
                .withProperty("second.base.url", "another-http-value")
                .withExpectedProperties(new MyExpectedApplicationProperties())
                .build();

        applicationProperties.validate();
    }

    @Test(expected = RuntimeException.class)
    public void exceptionOnMissingPropertyValue() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .withProperties(ServiceConfig.loadProperties("blank.properties"))
                .withExpectedProperties(() -> {
                    final HashSet<String> names = new HashSet<>();
                    names.add("BLANK");
                    return names;
                })
                .build();

        applicationProperties.validate();
    }

    @Test(expected = RuntimeException.class)
    public void exceptionOnMissingPropertyKey() {
        final ApplicationProperties applicationProperties = ApplicationProperties.Builder
                .builder()
                .withExpectedProperties(new MyExpectedApplicationProperties())
                .build();

        applicationProperties.validate();
    }

    private Properties getProperties(String key, String value) {
        final Properties properties = new Properties();
        properties.setProperty(key, value);
        return properties;
    }

    public class MyExpectedApplicationProperties implements ApiExpectedApplicationProperties {


        @Override
        public Set<String> getKeys() {
            return getApiKeys();
        }
    }

    public interface ApiExpectedApplicationProperties extends ExpectedApplicationProperties {

        default Set<String> getApiKeys(){
            final HashSet<String> names = new HashSet<>();
            names.add("base.url");
            return names;
        }
    }

}