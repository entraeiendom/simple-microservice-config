package no.cantara.config.store;

import no.cantara.config.SourceConfigurationLocationException;

import java.util.Map;

abstract class AbstractStore implements Store {
    private final SourceConfigurationLocationException location;

    AbstractStore(int i) {
        this.location = new SourceConfigurationLocationException(i);
    }

    public SourceConfigurationLocationException stackWhenConfigured() {
        return location;
    }

    @Override
    public String get(String key) {
        return doGet(key);
    }

    abstract String doGet(String key);

    public void putAllToMap(Map<String, String> map) {
        doPutAllToMap(map);
    }

    abstract void doPutAllToMap(Map<String, String> map);
}
