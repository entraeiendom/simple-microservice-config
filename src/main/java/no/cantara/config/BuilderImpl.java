package no.cantara.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class BuilderImpl implements ApplicationProperties.Builder {

    private static final Logger log = LoggerFactory.getLogger(BuilderImpl.class);

    private Properties properties;
    private Set<String> expectedApplicationProperties;
    private boolean enableEnvironmentVariables;

    public BuilderImpl() {
        enableEnvironmentVariables = false;
        properties = new Properties();
    }

    @Override
    public ApplicationProperties.Builder withExpectedProperties(Class... expectedApplicationProperties) {
        final Set<String> propertyNames = Arrays.stream(expectedApplicationProperties)
                .map(aClass -> {
                    final Set<String> fields = Arrays.stream(aClass.getDeclaredFields())
                            .filter(field -> field.getType() == String.class)
                            .map(field -> {
                                try {
                                    return (String) field.get(null);
                                } catch (IllegalAccessException e) {
                                    return "";
                                }
                            }).filter(s -> s.isEmpty())
                            .collect(Collectors.toSet());
                    return fields;
                }).flatMap(Collection::stream)
                .collect(Collectors.toSet());
        this.expectedApplicationProperties = propertyNames;
        return this;
    }

    @Override
    public ApplicationProperties.Builder withProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public ApplicationProperties.Builder enableEnvironmentVariables() {
        throw new RuntimeException("Environment variables not supported at the moment");
    }

    @Override
    public ApplicationProperties.Builder setProperty(String key, String value) {
        properties.setProperty(key, value);
        return this;
    }

    @Override
    public ApplicationProperties build() {
        if (enableEnvironmentVariables) {
            return new ApplicationProperties(properties, Optional.ofNullable(expectedApplicationProperties));
        } else {
            return new ApplicationProperties(properties, Optional.ofNullable(expectedApplicationProperties), System.getenv());
        }
    }
}
