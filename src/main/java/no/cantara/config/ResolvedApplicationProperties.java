package no.cantara.config;

import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Read environment variables from classpath, local config and environment.
 */
public class ResolvedApplicationProperties {
    private static final Logger log = getLogger(ResolvedApplicationProperties.class);
    private Properties properties;
    private Optional<ApplicationProperties> expectedApplicationProperties;

    public ResolvedApplicationProperties(Properties properties, Optional<ApplicationProperties> expectedApplicationProperties) {
        this.expectedApplicationProperties = expectedApplicationProperties;
        this.properties = properties;
    }


    public void validate() {
        if(expectedApplicationProperties.isPresent()){
            final Set<String> propertyNames = this.properties.stringPropertyNames();
            final List<String> undefinedProperties = expectedApplicationProperties.get().getExpectedProperties().stream().filter(expectedPropertyName -> !propertyNames.contains(expectedPropertyName)).collect(Collectors.toList());
            if(!undefinedProperties.isEmpty()){
                final String message = "Expected properties is not loaded "+undefinedProperties;
                log.error(message);
                throw new RuntimeException(message);
            }
        }else{
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

        static ResolvedApplicationProperties.Builder builder() {
            return new BuilderImpl();
        }

        Builder withExpectedProperties(ApplicationProperties applicationProperties);

        Builder withProperties(Properties properties);

        Builder withProperty(String key, String value);

        ResolvedApplicationProperties build();




    }

}

