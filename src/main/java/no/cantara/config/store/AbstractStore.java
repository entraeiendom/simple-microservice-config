package no.cantara.config.store;

import no.cantara.config.SourceConfigurationLocationException;

abstract class AbstractStore implements Store {
    private final SourceConfigurationLocationException location;

    AbstractStore(SourceConfigurationLocationException location) {
        this.location = location;
    }

    public SourceConfigurationLocationException stackWhenConfigured() {
        return location;
    }
}
