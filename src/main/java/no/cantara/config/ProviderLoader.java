package no.cantara.config;

import java.util.ServiceLoader;

public class ProviderLoader {

    public static <R, F extends ProviderFactory<R>> F factoryOf(String providerIdOrClassname, Class<F> factoryClazz) {
        ServiceLoader<F> loader = ServiceLoader.load(factoryClazz);
        for (F providerInitializer : loader) {
            String providerId = providerInitializer.alias();
            Class<?> providerClass = providerInitializer.providerClass();
            if (providerIdOrClassname.equals(providerId)
                    || providerIdOrClassname.equals(providerClass.getName())
                    || providerIdOrClassname.equals(providerClass.getSimpleName())
            ) {
                return providerInitializer;
            }
        }
        throw new RuntimeException("No " + factoryClazz.getSimpleName() + " provider found for providerIdOrClassname: " + providerIdOrClassname);
    }

    public static <R, T extends ProviderFactory<R>> R configure(ApplicationProperties applicationProperties, String providerIdOrClassname, Class<T> clazz) {
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        for (T providerInitializer : loader) {
            String providerId = providerInitializer.alias();
            Class<?> providerClass = providerInitializer.providerClass();
            if (providerIdOrClassname.equals(providerId)
                    || providerIdOrClassname.equals(providerClass.getName())
                    || providerIdOrClassname.equals(providerClass.getSimpleName())
            ) {
                return providerInitializer.create(applicationProperties);
            }
        }
        throw new RuntimeException("No " + clazz.getSimpleName() + " provider found for providerIdOrClassname: " + providerIdOrClassname);
    }
}
