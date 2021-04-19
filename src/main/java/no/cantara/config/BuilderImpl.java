package no.cantara.config;

import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public class BuilderImpl implements ApplicationProperties.Builder {

    private Properties properties;
    private Set<String> expectedApplicationProperties;
    private boolean enableEnvironmentVariables;

    public BuilderImpl() {
        enableEnvironmentVariables = false;
        properties = new Properties();
    }

    @Override
    public ApplicationProperties.Builder withExpectedProperties(ExpectedApplicationProperties expectedApplicationProperties) {
        this.expectedApplicationProperties = expectedApplicationProperties.getKeys();
        return this;
    }

    @Override
    public ApplicationProperties.Builder withProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public ApplicationProperties.Builder enableEnvironmentVariables() {
        throw new RuntimeException("Environment variables not supported at the moment");
    }

    @Override
    public ApplicationProperties.Builder setProperty(String key, String value) {
        properties.setProperty(key, value);
        return this;
    }

    @Override
    public ApplicationProperties build() {
        if(enableEnvironmentVariables){
            return new ApplicationProperties(properties, Optional.ofNullable(expectedApplicationProperties));
        }else
        {
            return new ApplicationProperties(properties, Optional.ofNullable(expectedApplicationProperties), System.getenv());
        }
    }
}
