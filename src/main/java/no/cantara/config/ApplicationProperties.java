package no.cantara.config;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read environment variables from classpath, local config and environment.
 */
public interface ApplicationProperties {

    Map<String, String> map();

    String get(String name);

    default String get(String name, String defaultValue) {
        String value = get(name);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    default int asInt(String name) {
        String value = get(name);
        return Integer.parseInt(value);
    }

    default int asInt(String name, int defaultValue) {
        String value = get(name);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return defaultValue;
    }

    default long asLong(String name) {
        String value = get(name);
        return Long.parseLong(value);
    }

    default long asLong(String name, long defaultValue) {
        String value = get(name);
        if (value != null) {
            return Long.parseLong(value);
        }
        return defaultValue;
    }

    default boolean asBoolean(String name) {
        String value = get(name);
        return Boolean.parseBoolean(value);
    }

    default boolean asBoolean(String name, boolean defaultValue) {
        String value = get(name);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    default double asDouble(String name) {
        String value = get(name);
        return Double.parseDouble(value);
    }

    default double asDouble(String name, double defaultValue) {
        String value = get(name);
        if (value != null) {
            return Double.parseDouble(value);
        }
        return defaultValue;
    }

    default String logObfuscatedProperties() {
        return logObfuscatedProperties(map());
    }

    static ApplicationProperties getInstance() {
        if (ApplicationPropertiesRepo.theInstance() == null) {
            throw new IllegalStateException("Cannot get ApplicationProperties-instance prior to Builder.init()");
        }
        return ApplicationPropertiesRepo.theInstance();
    }

    static Builder builder() {
        return new StoreBasedApplicationProperties.Builder();
    }

    static Builder builderWithDefaults() {
        return new StoreBasedApplicationProperties.Builder()
                .classpathPropertiesFile("application.properties")
                .filesystemPropertiesFile("local_override.properties");
    }

    interface Builder {

        Builder expectedProperties(Class... expectedApplicationProperties);

        Builder classpathPropertiesFile(String resourcePath);

        Builder filesystemPropertiesFile(String resourcePath);

        Builder map(Map<String, String> map);

        Builder enableEnvironmentVariables(String prefix);

        Builder enableSystemProperties();

        default Builder property(String name, String value) {
            return values()
                    .put(name, value)
                    .end();
        }

        ValueBuilder values();

        interface ValueBuilder {
            ValueBuilder put(String name, String value);

            default ValueBuilder put(String name, int value) {
                put(name, String.valueOf(value));
                return this;
            }

            default ValueBuilder put(String name, long value) {
                put(name, String.valueOf(value));
                return this;
            }

            default ValueBuilder put(String name, double value) {
                put(name, String.valueOf(value));
                return this;
            }

            default ValueBuilder put(String name, boolean value) {
                put(name, String.valueOf(value));
                return this;
            }

            Builder end();
        }

        /**
         * Builds this builder producing an {@link ApplicationProperties} instance.
         *
         * @return the built {@link ApplicationProperties} instance.
         */
        ApplicationProperties build();

        /**
         * Builds this builder by calling {@link #build()} to produce an {@link ApplicationProperties} instance which is
         * then assigned to the static singleton reference that can be retrieved by calling
         * {@link ApplicationProperties#getInstance()}.
         *
         * @return the {@link ApplicationProperties} instance built by calling the {@link #build()} method.
         * @throws StaticSingletonAlreadyInitializedException if the static singleton is already set before this method
         *                                                    was called.
         */
        default ApplicationProperties buildAndSetStaticSingleton() throws StaticSingletonAlreadyInitializedException {
            if (ApplicationPropertiesRepo.theInstance() != null) {
                throw new StaticSingletonAlreadyInitializedException();
            }
            ApplicationProperties instance = build();
            ApplicationPropertiesRepo.initInstance(instance);
            return instance;
        }
    }

    static String logObfuscatedProperties(Map<String, String> properties) {
        final Map<String, String> obfuscatedProperties = properties.entrySet().stream().map(
                entry -> {
                    final String key = entry.getKey();
                    final String value = entry.getValue();
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
}

