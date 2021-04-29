package no.cantara.config;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Internal class to encapsulate the correct access and mutation of statically scoped state.
 */
class ApplicationPropertiesRepo {

    /*
     * Singleton is protected by an atomic-reference to avoid any threading or memory-consistency issues.
     */
    private static final AtomicReference<ApplicationProperties> singletonRef = new AtomicReference<>();

    static ApplicationProperties theInstance() {
        return singletonRef.get();
    }

    static void initInstance(ApplicationProperties applicationProperties) {
        singletonRef.set(applicationProperties);
    }
}
