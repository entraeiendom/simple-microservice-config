package no.cantara.config.store;

import no.cantara.config.EnvironmentVariableEscaping;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

class EnvironmentStore extends AbstractStore {

    private static final Logger log = getLogger(EnvironmentStore.class);

    private final Set<String> casingSet;

    private final String prefix;
    private final boolean useEscaping;

    EnvironmentStore(String prefix, boolean useEscaping, Set<String> casingSet) {
        super(4);
        this.prefix = prefix;
        this.useEscaping = useEscaping;
        this.casingSet = casingSet;
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
    public String get(String key) {
        if (!key.toLowerCase().equals(key)) {
            // key has at least one uppercase letter
            if (!casingSet.contains(key)) {
                // no casing specified for key
                return null;
            }
        }
        String envKey = javaPropertyToEnvVar(key);
        String value = System.getenv(envKey);
        return value;
    }

    @Override
    public void putAllToMap(Map<String, String> map) {
        Map<String, String> casingByLowercase = casingSet.stream().collect(Collectors.toMap(String::toLowerCase, k -> k));
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                String strippedEnvVarKey = entry.getKey().substring(prefix.length());
                if (useEscaping) {
                    if (map.containsKey(strippedEnvVarKey)) {
                        log.warn("Environment-variable '{}' will NOT override property with same name. To override this property, use environment-variable '{}'", strippedEnvVarKey, EnvironmentVariableEscaping.escape(strippedEnvVarKey));
                    }
                }
                String propKey = envVarToJavaProperty(strippedEnvVarKey);
                String possiblyAliasedPropKey = casingByLowercase.getOrDefault(propKey, propKey);
                map.put(possiblyAliasedPropKey, entry.getValue());
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
