package no.cantara.config.store;

import no.cantara.config.SourceConfigurationLocationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

class MapStore extends AbstractStore {
    final Map<String, String> valueByKey = new LinkedHashMap<>();

    MapStore(SourceConfigurationLocationException location, Map<String, String> map, int i) {
        super(location);
        valueByKey.putAll(map);
    }

    @Override
    public String get(String key) {
        return valueByKey.get(key);
    }

    @Override
    public void putAllToMap(Map<String, String> map) {
        map.putAll(valueByKey);
    }

    @Override
    public String toString() {
        return "Map";
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
