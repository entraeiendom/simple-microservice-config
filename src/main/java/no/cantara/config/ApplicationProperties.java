package no.cantara.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Read environment variables from classpath, local config and environment.
 */
public class ApplicationProperties {
    private static final Logger log = getLogger(ApplicationProperties.class);
    private final Map<String, String> envVariables;
    private Properties properties;
    private Optional<Set<String>> expectedApplicationProperties;
    private static ApplicationProperties SINGELTON;

    private ApplicationProperties(Properties properties, Optional<Set<String>> expectedApplicationProperties) {
        this.expectedApplicationProperties = expectedApplicationProperties;
        this.properties = properties;
        envVariables = Collections.emptyMap();
    }


    private <T> ApplicationProperties(Properties properties, Optional<Set<String>> expectedApplicationProperties, Map<String, String> envVariables) {
        this.expectedApplicationProperties = expectedApplicationProperties;
        this.properties = properties;
        this.envVariables = envVariables;
    }

    public static ApplicationProperties getInstance() {
        if (SINGELTON == null) {
            throw new IllegalStateException("Cannot get ApplicationProperties-instance prior to Builder.init()");
        }
        return SINGELTON;
    }


    public void validate() {
        if (expectedApplicationProperties.isPresent()) {
            log.info("*********************");
            log.info("The application has resolved the following properties");
            log.info(logObfuscatedProperties());
            log.info("*********************");
            final Set<String> expectedKeys = expectedApplicationProperties.get();
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

    public Map<String, String> getMap() {
        return properties.entrySet().stream().collect(
                Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> String.valueOf(e.getValue()),
                        (prev, next) -> next, HashMap::new
                ));
    }

    public String logObfuscatedProperties() {
        final Map<Object, Object> obfuscatedProperties = properties.entrySet().stream().map(
                (Function<Map.Entry<Object, Object>, Map.Entry<Object, Object>>) entry -> {
                    final String key = (String) entry.getKey();
                    final String value = (String) entry.getValue();
                    final boolean isSecret = key.contains("secret") || key.contains("token") || key.contains("password");
                    if (isSecret) {
                        if (value.length() > 10) {
                            final String substring = value.substring(0, 2);
                            return new AbstractMap.SimpleEntry<>(key, substring + "******");
                        } else {
                            return new AbstractMap.SimpleEntry<>(key, "******");
                        }

                    } else {
                        return entry;
                    }
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return obfuscatedProperties.toString();
    }

    public interface Builder {

        static ApplicationProperties.Builder builder() {
            return new BuilderImpl();
        }

        Builder withExpectedProperties(Class... expectedApplicationProperties);

        Builder withProperties(Properties properties);

        Builder enableEnvironmentVariables();

        Builder setProperty(String key, String value);

        void init();

    }

    public static class BuilderImpl implements ApplicationProperties.Builder {

        private final Logger log = LoggerFactory.getLogger(BuilderImpl.class);

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
                                        log.info("Field with name {} is non-accessible", field.getName());
                                        return "";
                                    }
                                }).filter(s -> !s.isEmpty())
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
        public synchronized void init() {
            if (SINGELTON != null) {
                throw new IllegalStateException("Cannon initialize ApplicationProperties-singelton twice");
            }
            ApplicationProperties applicationProperties;
            final Optional<Set<String>> expectedApplicationProperties = Optional.ofNullable(this.expectedApplicationProperties);
            if (enableEnvironmentVariables) {
                applicationProperties = new ApplicationProperties(properties, expectedApplicationProperties);
            } else {
                applicationProperties = new ApplicationProperties(properties, expectedApplicationProperties, System.getenv());
            }
            if (expectedApplicationProperties.isPresent()) {
                applicationProperties.validate();
            }
            SINGELTON = applicationProperties;
        }
    }
}

