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

    static class DebuggableSource implements Source {

        final Store store;
        final String propertyName;

        DebuggableSource(Store store, String propertyName) {
            this.store = store;
            this.propertyName = propertyName;
        }

        @Override
        public String propertyName() {
            return propertyName;
        }

        @Override
        public String propertyValue() {
            return store.get(propertyName);
        }

        @Override
        public String description() {
            return store.toString();
        }

        @Override
        public SourceConfigurationLocationException stackTraceElement() {
            return store.stackWhenConfigured();
        }

        @Override
        public String toString() {
            return DebugUtils.debugSource(this);
        }
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

    Map<String, String> buildMapFromStore() {
        Map<String, String> map = new LinkedHashMap<>();
        Iterator<Store> it = storeList.descendingIterator();
        while (it.hasNext()) {
            Store store = it.next();
            store.putAllToMap(map);
        }
        return map;
    }

    private interface Store {
        /**
         * @param key
         * @return the value or null if this store has no value configured for key.
         */
        String get(String key);

        void putAllToMap(Map<String, String> map);

        SourceConfigurationLocationException stackWhenConfigured();
    }

    private abstract static class AbstractStore implements Store {
        private final SourceConfigurationLocationException location;

        AbstractStore(int i) {
            this.location = new SourceConfigurationLocationException(i);
        }

        public SourceConfigurationLocationException stackWhenConfigured() {
            return location;
        }
    }

    private static class ClasspathPropertiesStore extends AbstractStore {
        private final String resourcePath;
        private final Map<String, String> propertyByName = new LinkedHashMap<>();

        private List<URL> getPropertyResources() {
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
            super(4);
            this.resourcePath = resourcePath;

            // If classpath resource exists, read it
            List<URL> classPathResources = getPropertyResources();
            for (int i = classPathResources.size() - 1; i >= 0; i--) {
                URL classPathResource = classPathResources.get(i);
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

    private static class FilesystemPropertiesStore extends AbstractStore {
        private final String resourcePath;
        private final Map<String, String> propertyByName = new LinkedHashMap<>();

        private FilesystemPropertiesStore(String resourcePath) {
            super(4);
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

    private static class EnvironmentStore extends AbstractStore {
        private final String prefix;
        private final boolean useEscaping;

        private EnvironmentStore(String prefix, boolean useEscaping) {
            super(4);
            this.prefix = prefix;
            this.useEscaping = useEscaping;
        }

        public String envVarToJavaProperty(String envVarKey) {
            if (!useEscaping) {
                return envVarKey;
            }
            return EnvironmentVariableEscaping.unescape(envVarKey);
        }

        public String javaPropertyToEnvVar(String propKey) {
            if (!useEscaping) {
                return propKey;
            }
            return EnvironmentVariableEscaping.escape(propKey);
        }

        public String get(String key) {
            String envKey = prefix + javaPropertyToEnvVar(key);
            return System.getenv(envKey);
        }

        @Override
        public void putAllToMap(Map<String, String> map) {
            for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    String strippedEnvVarKey = entry.getKey().substring(prefix.length());
                    if (useEscaping) {
                        if (map.containsKey(strippedEnvVarKey)) {
                            log.warn("Environment-variable '{}' will NOT override property with same name. To override this property, use environment-variable '{}'", strippedEnvVarKey, EnvironmentVariableEscaping.escape(strippedEnvVarKey));
                        }
                    }
                    String propKey = envVarToJavaProperty(strippedEnvVarKey);
                    map.put(propKey, entry.getValue());
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

    private static class SystemPropertiesStore extends AbstractStore {
        private final String prefix;

        private SystemPropertiesStore(String prefix) {
            super(4);
            this.prefix = prefix;
        }

        public String get(String key) {
            return System.getProperty(prefix + key);
        }

        @Override
        public void putAllToMap(Map<String, String> map) {
            for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                    if (((String) entry.getKey()).startsWith(prefix)) {
                        String strippedKey = ((String) entry.getKey()).substring(prefix.length());
                        map.put(strippedKey, (String) entry.getValue());
                    }
                }
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

    private static class MapStore extends AbstractStore {
        final Map<String, String> valueByKey = new LinkedHashMap<>();

        private MapStore(Map<String, String> map, int i) {
            super(4 + i);
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
            storeList.addFirst(new MapStore(map, 0));
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
        public ApplicationProperties.Builder enableEnvironmentVariables() {
            storeList.addFirst(new EnvironmentStore("", true));
            return this;
        }

        @Override
        public ApplicationProperties.Builder enableEnvironmentVariables(String prefix) {
            storeList.addFirst(new EnvironmentStore(prefix, true));
            return this;
        }

        @Override
        public ApplicationProperties.Builder enableEnvironmentVariablesWithoutEscaping() {
            storeList.addFirst(new EnvironmentStore("", false));
            return this;
        }

        @Override
        public ApplicationProperties.Builder enableSystemProperties() {
            storeList.addFirst(new SystemPropertiesStore(""));
            return this;
        }

        @Override
        public ApplicationProperties.Builder enableSystemProperties(String prefix) {
            storeList.addFirst(new SystemPropertiesStore(prefix));
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
                    storeList.addFirst(new MapStore(map, 1));
                    return Builder.this;
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