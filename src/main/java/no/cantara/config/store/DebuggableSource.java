package no.cantara.config.store;

import no.cantara.config.ApplicationProperties;
import no.cantara.config.DebugUtils;
import no.cantara.config.SourceConfigurationLocationException;

class DebuggableSource implements ApplicationProperties.Source {

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
