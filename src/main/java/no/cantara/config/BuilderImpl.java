package no.cantara.config;

import java.util.Optional;
import java.util.Properties;

public class BuilderImpl implements ResolvedApplicationProperties.Builder {

    private Properties properties;
    private ApplicationProperties applicationProperties;

    public BuilderImpl() {
        properties = new Properties();
    }

    @Override
    public ResolvedApplicationProperties.Builder withExpectedProperties(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        return this;
    }

    @Override
    public ResolvedApplicationProperties.Builder withProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public ResolvedApplicationProperties.Builder withProperty(String key, String value) {
        properties.setProperty(key, value);
        return this;
    }

    @Override
    public ResolvedApplicationProperties build() {
        return new ResolvedApplicationProperties(properties, Optional.ofNullable(applicationProperties));
    }
}
