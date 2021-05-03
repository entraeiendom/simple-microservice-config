package no.cantara.config.store;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

class FilesystemPropertiesStore extends AbstractStore {
    private final String resourcePath;
    private final Map<String, String> propertyByName = new LinkedHashMap<>();

    FilesystemPropertiesStore(String resourcePath) {
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
        return "Filesystem based source of properties with resource-path: '" + resourcePath + "'";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilesystemPropertiesStore that = (FilesystemPropertiesStore) o;
        return Objects.equals(resourcePath, that.resourcePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourcePath);
    }
}
