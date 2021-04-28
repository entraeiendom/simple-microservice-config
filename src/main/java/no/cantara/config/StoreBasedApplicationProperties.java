package no.cantara.config;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
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
     * A very efficient immutable map with pre-resolved entries.
     */
    private final Map<String, String> effectiveProperties;

    private StoreBasedApplicationProperties(Deque<Store> storeList) {
        this.storeList = storeList;
        this.effectiveProperties = Collections.unmodifiableMap(buildMapFromStore());
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
    public List<String> sourcesOf(String name) {
        List<String> result = new ArrayList<>(storeList.size());
        for (Store store : storeList) {
            if (store.get(name) != null) {
                result.add(store.toString());
            }
        }
        return result;
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

    public String debugProperty(String name) {
        Iterator<Store> it = storeList.iterator();
        while (it.hasNext()) {
            Store store = it.next();
            String value = store.get(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private interface Store {
        /**
         * @param key
         * @return the value or null if this store has no value configured for key.
         */
        String get(String key);

        void putAllToMap(Map<String, String> map);
    }

    private static class ClasspathPropertiesStore implements Store {
        private final String resourcePath;
        private final Map<String, String> propertyByName = new LinkedHashMap<>();

        private Iterable<URL> getPropertyResources() {
            List<URL> resources = new ArrayList<>();
            Enumeration<URL> classPathResources;
            try {
                classPathResources = ClassLoader.getSystemResources(resourcePath);
                while (classPathResources.hasMoreElements()) {
                    resources.add(classPathResources.nextElement());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return resources;
        }

        private ClasspathPropertiesStore(String resourcePath) {
            this.resourcePath = resourcePath;

            // If classpath resource exists, read it
            Iterable<URL> classPathResources = getPropertyResources();
            classPathResources.forEach(classPathResource -> {
                Properties properties = new Properties();
                try {
                    URLConnection urlConnection = classPathResource.openConnection();
                    try (Reader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                        properties.load(reader);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                for (Map.Entry<Object, Object> e : properties.entrySet()) {
                    propertyByName.put((String) e.getKey(), (String) e.getValue());
                }
            });
        }

        public String get(String key) {
            return propertyByName.get(key);
        }

        @Override
        public void putAllToMap(Map<String, String> map) {
            map.putAll(propertyByName);
        }

        @Override
        public String toString() {
            return "Classpath based source of properties with resource-path: '" + resourcePath + "'";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClasspathPropertiesStore that = (ClasspathPropertiesStore) o;
            return Objects.equals(resourcePath, that.resourcePath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resourcePath);
        }
    }

    private static class FilesystemPropertiesStore implements Store {
        private final String resourcePath;
        private final Map<String, String> propertyByName = new LinkedHashMap<>();

        private FilesystemPropertiesStore(String resourcePath) {
            this.resourcePath = resourcePath;

            // If file exists, override configuration
            Path path = Paths.get(resourcePath);
            if (Files.exists(path)) {
                Properties properties = new Properties();
                try (Reader reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(path.toFile()), StandardCharsets.UTF_8))) {
                    properties.load(reader);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                for (Map.Entry<Object, Object> e : properties.entrySet()) {
                    propertyByName.put((String) e.getKey(), (String) e.getValue());
                }
            }
        }

        public String get(String key) {
            return propertyByName.get(key);
        }

        @Override
        public void putAllToMap(Map<String, String> map) {
            map.putAll(propertyByName);
        }

        @Override
        public String toString() {
            return "Filesystem based source of properties with resource-path: '" + resourcePath + "'";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClasspathPropertiesStore that = (ClasspathPropertiesStore) o;
            return Objects.equals(resourcePath, that.resourcePath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resourcePath);
        }
    }

    private static class EnvironmentStore implements Store {
        private final String prefix;

        private EnvironmentStore(String prefix) {
            this.prefix = prefix;
        }

        public String get(String key) {
            return System.getenv(prefix + key);
        }

        @Override
        public void putAllToMap(Map<String, String> map) {
            for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    map.put(entry.getKey().substring(prefix.length()), entry.getValue());
                }
            }
        }

        @Override
        public String toString() {
            return "Environment based source with prefix: '" + prefix + "'";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EnvironmentStore that = (EnvironmentStore) o;
            return Objects.equals(prefix, that.prefix);
        }

        @Override
        public int hashCode() {
            return Objects.hash(prefix);
        }
    }

    private static class SystemPropertiesStore implements Store {
        private SystemPropertiesStore() {
        }

        public String get(String key) {
            return System.getProperty(key);
        }

        @Override
        public void putAllToMap(Map<String, String> map) {
            for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                map.put((String) entry.getKey(), (String) entry.getValue());
            }
        }

        @Override
        public String toString() {
            return "SystemProperties based source";
        }

        @Override
        public int hashCode() {
            return 31 * 7385;
        }

        @Override
        public boolean equals(Object o) {
            return o != null && getClass() == o.getClass();
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

    private static class MapStore implements Store {
        final Map<String, String> valueByKey = new LinkedHashMap<>();

        private MapStore(Map<String, String> map) {
            valueByKey.putAll(map);
        }

        public String get(String key) {
            return valueByKey.get(key);
        }

        @Override
        public void putAllToMap(Map<String, String> map) {
            map.putAll(valueByKey);
        }

        @Override
        public String toString() {
            return "Map based source";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MapStore that = (MapStore) o;
            return Objects.equals(valueByKey, that.valueByKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(valueByKey);
        }
    }

    public static class Builder implements ApplicationProperties.Builder {
        final Deque<Store> storeList = new LinkedList<>();
        final Set<String> expectedApplicationProperties = new LinkedHashSet<>();

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
            storeList.addFirst(new MapStore(map));
            return this;
        }

        @Override
        public ApplicationProperties.Builder classpathPropertiesFile(String resourcePath) {
            storeList.addFirst(new ClasspathPropertiesStore(resourcePath));
            return this;
        }

        @Override
        public ApplicationProperties.Builder filesystemPropertiesFile(String resourcePath) {
            storeList.addFirst(new FilesystemPropertiesStore(resourcePath));
            return this;
        }

        @Override
        public ApplicationProperties.Builder enableEnvironmentVariables(String prefix) {
            storeList.addFirst(new EnvironmentStore(prefix));
            return this;
        }

        @Override
        public ApplicationProperties.Builder enableSystemProperties() {
            storeList.addFirst(new SystemPropertiesStore());
            return this;
        }

        @Override
        public ValueBuilder values() {
            return new ValueBuilder() {
                final Map<String, String> map = new LinkedHashMap<>();

                @Override
                public ValueBuilder put(String name, String value) {
                    map.put(name, value);
                    return this;
                }

                @Override
                public ApplicationProperties.Builder end() {
                    return map(map);
                }
            };
        }

        @Override
        public StoreBasedApplicationProperties build() {
            StoreBasedApplicationProperties applicationProperties = new StoreBasedApplicationProperties(storeList);
            validate(applicationProperties.effectiveProperties);
            return applicationProperties;
        }
    }
}