package no.cantara.config.store;

import no.cantara.config.ApplicationProperties;
import no.cantara.config.SourceConfigurationLocationException;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class StoreBasedApplicationProperties implements ApplicationProperties {

    private static final Logger log = getLogger(StoreBasedApplicationProperties.class);

    /*
     * The original list of property-sources. Useful for debugging what source a configuration came from.
     */
    private final Deque<Store> storeList;

    /*
     * A very performant immutable map with pre-resolved entries.
     */
    private final Map<String, String> effectiveProperties;

    private final NavigableMap navigableMap;

    private StoreBasedApplicationProperties(Deque<Store> storeList) {
        this.storeList = storeList;
        this.effectiveProperties = Collections.unmodifiableMap(buildMapFromStore());
        navigableMap = Collections.unmodifiableNavigableMap(new TreeMap<>(effectiveProperties));
    }

    @Override
    public Map<String, String> map() {
        return effectiveProperties;
    }

    @Override
    public String get(String name) {
        return effectiveProperties.get(name);
    }

    @Override
    public List<Source> sourcesOf(String name) {
        List<Source> result = new ArrayList<>(storeList.size());
        for (Store store : storeList) {
            if (store.get(name) != null) {
                result.add(new DebuggableSource(store, name));
            }
        }
        return result;
    }

    @Override
    public Map<String, String> subMap(String prefix) {
        Map<String, String> subMap = new LinkedHashMap<>();
        NavigableMap<String, String> subMapWithPrefix = this.navigableMap.subMap(prefix, true, prefix + Character.MAX_VALUE, true);
        for (Map.Entry<String, String> entry : subMapWithPrefix.entrySet()) {
            subMap.put(entry.getKey().substring(prefix.length()), entry.getValue());
        }
        return subMap;
    }

    Map<String, String> buildMapFromStore() {
        Map<String, String> map = new LinkedHashMap<>();
        Iterator<Store> it = storeList.descendingIterator();
        while (it.hasNext()) {
            Store store = it.next();
            store.putAllToMap(map);
        }
        return map;
    }

    public static class Builder implements ApplicationProperties.Builder {
        final Deque<Store> storeList = new LinkedList<>();
        final Set<String> expectedApplicationProperties = new LinkedHashSet<>();
        final Set<String> envVarCasingSet = new LinkedHashSet<>();

        private void validate(Map<String, String> properties) {
            if (expectedApplicationProperties.size() > 0) {
                log.info("*********************");
                log.info("The application has resolved the following properties");
                log.info(ApplicationProperties.logObfuscatedProperties(properties));
                log.info("*********************");
                final Set<String> expectedKeys = expectedApplicationProperties;
                final List<String> undefinedProperties = expectedKeys.stream().filter(expectedPropertyName -> !properties.containsKey(expectedPropertyName)).collect(toList());
                if (!undefinedProperties.isEmpty()) {
                    final String message = "Expected properties is not loaded " + undefinedProperties;
                    log.error(message);
                    throw new RuntimeException(message);
                }
                final List<String> undefinedValues = expectedKeys.stream()
                        .filter(expectedPropertyName ->
                                properties.get(expectedPropertyName) == null || properties.get(expectedPropertyName).isEmpty()
                        ).collect(toList());
                if (!undefinedValues.isEmpty()) {
                    final String message = "Expected properties is defined without value " + undefinedValues;
                    log.error(message);
                    throw new RuntimeException(message);
                }
                final List<String> additionalProperties = properties.keySet().stream().filter(s -> !expectedKeys.contains(s)).collect(toList());
                if (!additionalProperties.isEmpty()) {
                    log.warn("The following properties are loaded but not defined as expected for the application {}", additionalProperties);
                }

            }
        }

        @Override
        public ApplicationProperties.Builder envVarCasing(String name) {
            envVarCasingSet.add(name);
            return this;
        }

        @Override
        public ApplicationProperties.Builder expectedProperties(Class... expectedApplicationProperties) {
            final List<String> propertyNames = Arrays.stream(expectedApplicationProperties)
                    .map(aClass -> {
                        final Set<String> fields = Arrays.stream(aClass.getDeclaredFields())
                                .filter(field -> field.getType() == String.class)
                                .map(field -> {
                                    try {
                                        return (String) field.get(null);
                                    } catch (IllegalAccessException e) {
                                        log.warn("Field with name {} is non-accessible", field.getName());
                                        return "";
                                    }
                                }).filter(s -> !s.isEmpty())
                                .collect(Collectors.toSet());
                        return fields;
                    }).flatMap(Collection::stream)
                    .collect(Collectors.toList());
            this.expectedApplicationProperties.addAll(propertyNames);
            return this;
        }

        @Override
        public ApplicationProperties.Builder map(Map<String, String> map) {
            storeList.addFirst(new MapStore(new SourceConfigurationLocationException(1), map, 0));
            return this;
        }

        @Override
        public ApplicationProperties.Builder classpathPropertiesFile(String resourcePath) {
            storeList.addFirst(new ClasspathPropertiesStore(new SourceConfigurationLocationException(1), resourcePath));
            return this;
        }

        @Override
        public ApplicationProperties.Builder filesystemPropertiesFile(String resourcePath) {
            storeList.addFirst(new FilesystemPropertiesStore(new SourceConfigurationLocationException(1), resourcePath));
            return this;
        }

        @Override
        public ApplicationProperties.Builder enableEnvironmentVariables() {
            Set<String> basePropertyKeys = new StoreBasedApplicationProperties(new LinkedList<>(storeList)).map().keySet();
            storeList.addFirst(new EnvironmentStore(basePropertyKeys, new SourceConfigurationLocationException(1), "", true, envVarCasingSet));
            return this;
        }

        @Override
        public ApplicationProperties.Builder enableEnvironmentVariables(String prefix) {
            Set<String> basePropertyKeys = new StoreBasedApplicationProperties(new LinkedList<>(storeList)).map().keySet();
            storeList.addFirst(new EnvironmentStore(basePropertyKeys, new SourceConfigurationLocationException(1), prefix, true, envVarCasingSet));
            return this;
        }

        @Override
        public ApplicationProperties.Builder enableEnvironmentVariablesWithoutEscaping() {
            Set<String> basePropertyKeys = new StoreBasedApplicationProperties(new LinkedList<>(storeList)).map().keySet();
            storeList.addFirst(new EnvironmentStore(basePropertyKeys, new SourceConfigurationLocationException(1), "", false, envVarCasingSet));
            return this;
        }

        @Override
        public ApplicationProperties.Builder enableSystemProperties() {
            Set<String> basePropertyKeys = new StoreBasedApplicationProperties(new LinkedList<>(storeList)).map().keySet();
            storeList.addFirst(new SystemPropertiesStore(basePropertyKeys, new SourceConfigurationLocationException(1), ""));
            return this;
        }

        @Override
        public ApplicationProperties.Builder enableSystemProperties(String prefix) {
            Set<String> basePropertyKeys = new StoreBasedApplicationProperties(new LinkedList<>(storeList)).map().keySet();
            storeList.addFirst(new SystemPropertiesStore(basePropertyKeys, new SourceConfigurationLocationException(1), prefix));
            return this;
        }

        @Override
        public ApplicationProperties.Builder property(String name, String value) {
            return new ValueBuilderStore(new SourceConfigurationLocationException(1))
                    .put(name, value)
                    .end();
        }

        @Override
        public ValueBuilder values() {
            return new ValueBuilderStore(new SourceConfigurationLocationException(1));
        }

        class ValueBuilderStore implements ValueBuilder {
            final SourceConfigurationLocationException locationException;
            final Map<String, String> map = new LinkedHashMap<>();

            ValueBuilderStore(SourceConfigurationLocationException locationException) {
                this.locationException = locationException;
            }

            @Override
            public ValueBuilder put(String name, String value) {
                map.put(name, value);
                return this;
            }

            @Override
            public ApplicationProperties.Builder end() {
                storeList.addFirst(new MapStore(locationException, map, 1));
                return Builder.this;
            }
        }

        @Override
        public StoreBasedApplicationProperties build() {
            StoreBasedApplicationProperties applicationProperties = new StoreBasedApplicationProperties(storeList);
            validate(applicationProperties.effectiveProperties);
            return applicationProperties;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreBasedApplicationProperties that = (StoreBasedApplicationProperties) o;
        return Objects.equals(storeList, that.storeList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeList);
    }
}