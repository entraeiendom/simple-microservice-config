package no.cantara.config.store;

import no.cantara.config.EnvironmentVariableEscaping;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

class EnvironmentStore extends AbstractStore {

    private static final Logger log = getLogger(EnvironmentStore.class);

    private final String prefix;
    private final boolean useEscaping;

    EnvironmentStore(String prefix, boolean useEscaping) {
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

    @Override
    String doGet(String key) {
        String envKey = prefix + javaPropertyToEnvVar(key);
        return System.getenv(envKey);
    }

    @Override
    void doPutAllToMap(Map<String, String> map) {
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
