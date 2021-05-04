package no.cantara.config.testsupport;

/**
 * Test helper util to allow setting a mutable static singleton so that it can be re-assigned between tests.
 */
public class ApplicationPropertiesTestHelper {

    private static class MutableSingleton {
        static final MutableDelegatingApplicationProperties.DelegatingBuilder builder = new MutableDelegatingApplicationProperties.DelegatingBuilder();
        static final MutableDelegatingApplicationProperties instance = builder.build();
    }

    public static MutableDelegatingApplicationProperties getInstance() {
        return MutableSingleton.instance;
    }

    /**
     * Enables the use of a mutable (but slower) singleton suitable for testing where there may be need to re-assign
     * the ApplicationProperties singleton.
     * <p>
     * Creates a mutable ApplicationProperties instance that is set as the static singleton and that allows a switch of
     * its internal delegate after creation. This is useful for unit-testing where you may need to change configuration
     * between some of the tests. The resulting instance is mutable but therefore not as fast as a non-test instance is
     * due to need of synchronization mechanisms that guarantee happens-before semantics.
     */
    public static void enableMutableSingleton() {
        MutableSingleton.builder.buildAndSetStaticSingleton();
    }

    /**
     * This method can be used to clear the static singleton. This can be useful for testing when you want to assure
     * that no static singleton is leaked between tests.
     */
    public static void clearSingleton() {
        getInstance().setDelegate(null);
    }
}
