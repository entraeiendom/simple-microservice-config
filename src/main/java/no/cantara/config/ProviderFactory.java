package no.cantara.config;

public interface ProviderFactory<R> {

    Class<?> providerClass();

    String alias();

    R create(ApplicationProperties configuration);
}
