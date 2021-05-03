package no.cantara.config.store;

import java.util.Map;

class SystemPropertiesStore extends AbstractStore {
    private final String prefix;

    SystemPropertiesStore(String prefix) {
        super(4);
        this.prefix = prefix;
    }

    @Override
    String doGet(String key) {
        return System.getProperty(prefix + key);
    }

    @Override
    void doPutAllToMap(Map<String, String> map) {
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
