package no.cantara.config.testsupport;

import no.cantara.config.ApplicationProperties;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A mutable delegating application-properties class that should only be used for testing. The purpose of this class
 * is to allow mutation of an otherwise immutable instance.
 */
public class MutableDelegatingApplicationProperties implements ApplicationProperties {

    private final AtomicReference<ApplicationProperties> delegateRef = new AtomicReference<>();

    MutableDelegatingApplicationProperties() {
    }

    public void setDelegate(ApplicationProperties delegate) {
        delegateRef.set(delegate);
    }

    public ApplicationProperties getDelegate() {
        return delegateRef.get();
    }

    @Override
    public Map<String, String> map() {
        return delegateRef.get().map();
    }

    @Override
    public String get(String name) {
        return delegateRef.get().get(name);
    }

    @Override
    public List<Source> sourcesOf(String name) {
        return delegateRef.get().sourcesOf(name);
    }

    @Override
    public Map<String, String> subMap(String prefix) {
        return delegateRef.get().subMap(prefix);
    }

    public static class DelegatingBuilder implements ApplicationProperties.Builder {

        final AtomicReference<MutableDelegatingApplicationProperties> instanceRef = new AtomicReference<>();

        @Override
        public Builder expectedProperties(Class... expectedApplicationProperties) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder classpathPropertiesFile(String resourcePath) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder filesystemPropertiesFile(String resourcePath) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder map(Map<String, String> map) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder enableEnvironmentVariables() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder enableEnvironmentVariables(String prefix) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder enableEnvironmentVariablesWithoutEscaping() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder envVarCasing(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder enableSystemProperties() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder enableSystemProperties(String prefix) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder property(String name, String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ValueBuilder values() {
            throw new UnsupportedOperationException();
        }

        @Override
        public MutableDelegatingApplicationProperties build() {
            MutableDelegatingApplicationProperties instance = instanceRef.get();
            if (instance == null) {
                instance = new MutableDelegatingApplicationProperties();
                instanceRef.set(instance);
            }
            return instance;
        }
    }
}

