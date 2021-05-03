package no.cantara.config.store;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

class ClasspathPropertiesStore extends AbstractStore {
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

    ClasspathPropertiesStore(String resourcePath) {
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

    @Override
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
