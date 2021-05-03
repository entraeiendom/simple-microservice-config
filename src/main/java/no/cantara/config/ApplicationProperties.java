package no.cantara.config;

import no.cantara.config.store.StoreBasedApplicationProperties;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An efficient configuration class for building an immutable property-map based on different property sources. It is
 * guaranteed that any successful call to {@link Builder#buildAndSetStaticSingleton()} "happens-before" any subsequent
 * calls from any other thread to {@link #getInstance()}. Calls to {@link #map()} or {@link #get(String)} or any other
 * method for extracting effective property values are only reading from effective read-only data-structures and hence
 * do not need to be synchronized and are not synchronized internally by classes that implement this interface. So the
 * only cost of extracting property values using instances of this interface is one read operation to a
 * {@link java.util.LinkedHashMap} wrapped with a {@link Collections#unmodifiableMap(Map)}.
 */
public interface ApplicationProperties {

    /**
     * Get a map containing all effective properties. The returned map is immutable, and any attempts to mutate it will
     * result a runtime-exception.
     *
     * @return an immutable map with all effective properties.
     */
    Map<String, String> map();

    /**
     * Get the effective value of the property with the given name.
     *
     * @param name
     * @return the effective value
     */
    String get(String name);

    /**
     * Get the all configuration sources that provide a value for the given property. The first value in the returned
     * list is the effective value of the property. The next value in the list is the value that would have been the
     * effective value if the source of the first value in the list had not been used, and so on.
     * <p>
     * The intended use for this tool is debugging of configuration issues. It is not intended to be used by application
     * logic to differentiate on configuration.
     *
     * @param name the name of the property to get the sources of.
     * @return a list (possibly empty) of all the sources that provide values for the property with the given name.
     */
    List<Source> sourcesOf(String name);

    interface Source {
        String propertyName();

        String propertyValue();

        String description();

        SourceConfigurationLocationException stackTraceElement();
    }

    /**
     * Return a string suitable for debugging sources of all properties. If the parameter to debug overridden sources
     * is set, then all sources of a property is printed in string, otherwise only effective source for any given
     * property is printed.
     *
     * @param debugOverriddenSources whether to include non-effective sources for debugged properties
     * @return a string that can be read by humans for debugging source configuration of properties.
     */
    default String debugAll(boolean debugOverriddenSources) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> property : map().entrySet()) {
            DebugUtils.debugSources(sourcesOf(property.getKey()), debugOverriddenSources, sb);
        }
        return sb.toString();
    }

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
        return builder()
                .classpathPropertiesFile("application.properties")
                .filesystemPropertiesFile("local_override.properties")
                .enableSystemProperties()
                .enableEnvironmentVariables();
    }

    static Builder builderWithTestDefaults() {
        return builder()
                .classpathPropertiesFile("application.properties")
                .classpathPropertiesFile("test_override.properties")
                .enableSystemProperties();
    }

    static Builder builderWithTestDefaultsAndEnvironmentVariables() {
        return builderWithTestDefaults()
                .enableEnvironmentVariables();
    }

    /**
     * The order of property sources specified when using methods in this builder is significant for resolving effective
     * values of properties from the {@link ApplicationProperties} instance that is built. Sources configured later are
     * always used before an earlier configured source when resolving property values.
     */
    interface Builder {

        Builder expectedProperties(Class... expectedApplicationProperties);

        Builder classpathPropertiesFile(String resourcePath);

        Builder filesystemPropertiesFile(String resourcePath);

        Builder map(Map<String, String> map);

        Builder enableEnvironmentVariables();

        Builder enableEnvironmentVariables(String prefix);

        Builder enableEnvironmentVariablesWithoutEscaping();

        Builder envVarCasing(String name);

        Builder enableSystemProperties();

        Builder enableSystemProperties(String prefix);

        Builder property(String name, String value);

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

