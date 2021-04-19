package no.cantara.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
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
        final Set<String> propertyNames = Arrays.stream(expectedApplicationProperties).map(new Function<Class, Set<String>>() {
            @Override
            public Set<String> apply(Class aClass) {
                final Set<String> fields = Arrays.stream(aClass.getDeclaredFields()).filter(new Predicate<Field>() {
                    @Override
                    public boolean test(Field field) {
                        return field.getType() == String.class;
                    }
                }).map(new Function<Field, String>() {
                    @Override
                    public String apply(Field field) {
                        try {
                            return (String) field.get(null);
                        } catch (IllegalAccessException e) {
                            log.error("This should be filtered out before!");
                            return "";
                        }

                    }
                }).collect(Collectors.toSet());
                return fields;
            }
        }).flatMap(Collection::stream).collect(Collectors.toSet());
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
        if(enableEnvironmentVariables){
            return new ApplicationProperties(properties, Optional.ofNullable(expectedApplicationProperties));
        }else
        {
            return new ApplicationProperties(properties, Optional.ofNullable(expectedApplicationProperties), System.getenv());
        }
    }
}
