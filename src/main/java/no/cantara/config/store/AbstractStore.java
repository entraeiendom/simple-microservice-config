package no.cantara.config.store;

import no.cantara.config.SourceConfigurationLocationException;

abstract class AbstractStore implements Store {
    private final SourceConfigurationLocationException location;

    AbstractStore(int i) {
        this.location = new SourceConfigurationLocationException(i);
    }

    public SourceConfigurationLocationException stackWhenConfigured() {
        return location;
    }
}
