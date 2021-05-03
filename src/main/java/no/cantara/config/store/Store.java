package no.cantara.config.store;

import no.cantara.config.SourceConfigurationLocationException;

import java.util.Map;

interface Store {

    /**
     * @param key
     * @return the value or null if this store has no value configured for key.
     */
    String get(String key);

    void putAllToMap(Map<String, String> map);

    SourceConfigurationLocationException stackWhenConfigured();
}
