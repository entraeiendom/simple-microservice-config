package no.cantara.config;

import java.util.Optional;
import java.util.Properties;

public class BuilderImpl implements ApplicationProperties.Builder {

    private Properties properties;
    private ExpectedApplicationProperties expectedApplicationProperties;

    public BuilderImpl() {
        properties = new Properties();
    }

    @Override
    public ApplicationProperties.Builder withExpectedProperties(ExpectedApplicationProperties expectedApplicationProperties) {
        this.expectedApplicationProperties = expectedApplicationProperties;
        return this;
    }

    @Override
    public ApplicationProperties.Builder withProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public ApplicationProperties.Builder withProperty(String key, String value) {
        properties.setProperty(key, value);
        return this;
    }

    @Override
    public ApplicationProperties build() {
        return new ApplicationProperties(properties, Optional.ofNullable(expectedApplicationProperties));
    }
}
