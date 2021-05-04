package no.cantara.config.store;

import no.cantara.config.SourceConfigurationLocationException;

import java.util.Map;
import java.util.Set;

class SystemPropertiesStore extends AbstractStore {

    private final Set<String> basePropertyKeys;
    private final String prefix;

    SystemPropertiesStore(Set<String> basePropertyKeys, SourceConfigurationLocationException location, String prefix) {
        super(location);
        this.basePropertyKeys = basePropertyKeys;
        this.prefix = prefix;
    }

    @Override
    public String get(String key) {
        if (!basePropertyKeys.contains(key)) {
            return null; // key does not override an existing property from a base store
        }
        return System.getProperty(prefix + key);
    }

    @Override
    public void putAllToMap(Map<String, String> map) {
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                if (((String) entry.getKey()).startsWith(prefix)) {
                    String strippedKey = ((String) entry.getKey()).substring(prefix.length());
                    if (basePropertyKeys.contains(strippedKey)) {
                        map.put(strippedKey, (String) entry.getValue());
                    } else {
                        // not an override, filter out
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        if (prefix == null || prefix.isEmpty()) {
            return "System-properties";
        }
        return "System-properties '" + prefix + "*'";
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
