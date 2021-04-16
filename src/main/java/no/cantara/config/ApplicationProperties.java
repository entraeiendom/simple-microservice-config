package no.cantara.config;

import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Read environment variables from classpath, local config and environment.
 */
public class ApplicationProperties {
    private static final Logger log = getLogger(ApplicationProperties.class);
    private Properties properties;
    private Optional<ExpectedApplicationProperties> expectedApplicationProperties;

    public ApplicationProperties(Properties properties, Optional<ExpectedApplicationProperties> expectedApplicationProperties) {
        this.expectedApplicationProperties = expectedApplicationProperties;
        this.properties = properties;
    }


    public void validate() {
        if (expectedApplicationProperties.isPresent()) {
            log.info("*********************");
            log.info("The application has resolved the following properties");
            log.info(properties.toString());
            log.info("*********************");
            final Set<String> expectedKeys = expectedApplicationProperties.get().getKeys();
            final List<String> undefinedProperties = expectedKeys.stream().filter(expectedPropertyName -> !properties.containsKey(expectedPropertyName)).collect(toList());
            if (!undefinedProperties.isEmpty()) {
                final String message = "Expected properties is not loaded " + undefinedProperties;
                log.error(message);
                throw new RuntimeException(message);
            }
            final List<String> undefinedValues = expectedKeys.stream()
                    .filter(expectedPropertyName ->
                            properties.getProperty(expectedPropertyName) == null || properties.getProperty(expectedPropertyName).isEmpty()
                    ).collect(toList());
            if (!undefinedValues.isEmpty()) {
                final String message = "Expected properties is defined without value " + undefinedValues;
                log.error(message);
                throw new RuntimeException(message);
            }
            final List<String> additionalProperties = properties.stringPropertyNames().stream().filter(s -> !expectedKeys.contains(s)).collect(toList());
            if (!additionalProperties.isEmpty()) {
                log.warn("The following properties are loaded but not defined as expected for the application {}", additionalProperties);
            }

        } else {
            throw new IllegalStateException("Expected application properties is not defined and as such cannot be validated");
        }
    }

    public String get(String name) {
        return properties.getProperty(name);
    }

    public Properties getProperties() {
        return properties;
    }

    public interface Builder {

        static ApplicationProperties.Builder builder() {
            return new BuilderImpl();
        }

        Builder withExpectedProperties(ExpectedApplicationProperties expectedApplicationProperties);

        Builder withProperties(Properties properties);

        Builder withProperty(String key, String value);

        ApplicationProperties build();


    }

}

