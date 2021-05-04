package no.cantara.config;

import no.cantara.config.testsupport.MutableDelegatingApplicationProperties;

/**
 * Internal class used to initialize and possibly re-assign the global singleton.
 */
class ApplicationPropertiesRepo {

    /*
     * Must be set before accessing the inner Singleton class.
     */
    static ApplicationProperties temporaryInstance;

    static class Singleton {
        static final ApplicationProperties instance;

        static {
            instance = temporaryInstance;
        }

        public static void init() {
        }
    }

    static ApplicationProperties theInstance() {
        return Singleton.instance;
    }

    static void initInstance(ApplicationProperties applicationProperties) {
        temporaryInstance = applicationProperties;
        Singleton.init(); // force assignment to static final singleton field
        temporaryInstance = null;
        if (Singleton.instance == null) {
            throw new StaticSingletonAlreadyInitializedException("Someone called ApplicationProperties.getInstance() before any instance was built and set as static singleton. Please build and set static singleton first");
        }
        if (Singleton.instance != applicationProperties) {
            // attempt was made to re-assign singleton, allow a workaround if we have a mutable singleton
            if (Singleton.instance instanceof MutableDelegatingApplicationProperties) {
                // re-assign the internal delegate of the singleton
                MutableDelegatingApplicationProperties mutableInstance = (MutableDelegatingApplicationProperties) Singleton.instance;
                if (applicationProperties instanceof MutableDelegatingApplicationProperties) {
                    // this is a call from ApplicationPropertiesTestHelper.enableMutableSingleton()
                    // it is not the first call, and its purpose is to ensure that the singleton is mutable, which it is.
                    // all is good, ignore.
                } else {
                    mutableInstance.setDelegate(applicationProperties);
                }
            } else {
                throw new StaticSingletonAlreadyInitializedException("Failed to re-assign the fast immutable production ready singleton. If you are testing, first call ApplicationPropertiesTestHelper.enableMutableSingleton() once to enable re-assignment of the singleton");
            }
        }
    }
}
