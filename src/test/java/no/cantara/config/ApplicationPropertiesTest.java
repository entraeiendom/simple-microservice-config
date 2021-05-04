package no.cantara.config;

import no.cantara.config.testsupport.ApplicationPropertiesTestHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationPropertiesTest {

    @BeforeClass
    public static void enableMutableSingleton() {
        ApplicationPropertiesTestHelper.enableMutableSingleton();
    }



    @Test
    public void shouldGetPropertiesWithSingleProperty() {
        ApplicationProperties.builder()
                .property("base.url", "http-value")
                .buildAndSetStaticSingleton();
        ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
        assertThat(applicationProperties.get("base.url")).isEqualTo("http-value");

        assertThat(applicationProperties.map()).containsEntry("base.url", "http-value");

    }

    @Test
    public void shouldGetPropertiesWithPropertiesSet() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("first.key", "first.value");
        map.put("second.key", "second.value");
        ApplicationProperties.builder()
                .map(map)
                .buildAndSetStaticSingleton();
        ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
        assertThat(applicationProperties.get("first.key")).isEqualTo("first.value");
        assertThat(applicationProperties.get("second.key")).isEqualTo("second.value");

        assertThat(applicationProperties.map())
                .containsEntry("first.key", "first.value")
                .containsEntry("second.key", "second.value")
                .hasSize(2);
    }

    @Test
    public void orderMatters_withMapOverridesOnlyMatchingProperties() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("first.key", "first.value");
        map.put("second.key", "second.value");
        map.put("third.key", "third.value");
        ApplicationProperties.builder()
                .property("first.property", "original.first.value")
                .property("third.key", "original.third.value")
                .map(map)
                .buildAndSetStaticSingleton();
        ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
        assertThat(applicationProperties.get("first.key")).isEqualTo("first.value");
        assertThat(applicationProperties.get("second.key")).isEqualTo("second.value");
        assertThat(applicationProperties.get("third.key")).isEqualTo("third.value");
        assertThat(applicationProperties.get("first.property")).isEqualTo("original.first.value");

        assertThat(applicationProperties.map())
                .containsEntry("first.key", "first.value")
                .containsEntry("second.key", "second.value")
                .containsEntry("third.key", "third.value")
                .containsEntry("first.property", "original.first.value")
                .hasSize(4);
    }

    @Test
    public void orderMatters_withPropertyAdds() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("first.key", "first.value");
        map.put("second.key", "second.value");
        ApplicationProperties.builder()
                .map(map)
                .property("last.property", "last.value")
                .buildAndSetStaticSingleton();
        ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
        assertThat(applicationProperties.get("first.key")).isEqualTo("first.value");
        assertThat(applicationProperties.get("second.key")).isEqualTo("second.value");
        assertThat(applicationProperties.get("last.property")).isEqualTo("last.value");

        assertThat(applicationProperties.map())
                .containsEntry("first.key", "first.value")
                .containsEntry("second.key", "second.value")
                .containsEntry("last.property", "last.value")
                .hasSize(3);
    }

    @Test
    public void orderMatters_withPropertyCanOverride() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("first.key", "first.value");
        map.put("second.key", "second.value");
        ApplicationProperties.builder()
                .map(map)
                .property("second.key", "different.value")
                .buildAndSetStaticSingleton();
        ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
        assertThat(applicationProperties.get("first.key")).isEqualTo("first.value");
        assertThat(applicationProperties.get("second.key")).isEqualTo("different.value");

        assertThat(applicationProperties.map())
                .containsEntry("first.key", "first.value")
                .containsEntry("second.key", "different.value")
                .hasSize(2);
    }

    @Test
    public void loggingPropertyValuesIsObfuscated() {
        final String longSecret = "youshouldnotseeme";
        final String shortSecret = "10tokensxx";
        final String publicValue = "an-url";
        ApplicationProperties.builder()
                .property("base", publicValue)
                .property("secret_so_much", longSecret)
                .property("a.secret", longSecret)
                .property("secret_so_much", longSecret)
                .property("postgres.password", longSecret)
                .property("slack_token", longSecret)
                .property("token_short", shortSecret)
                .buildAndSetStaticSingleton();
        ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
        assertThat(applicationProperties.logObfuscatedProperties())
                .doesNotContain(longSecret)
                .doesNotContain(shortSecret)
                .contains(publicValue)
                .contains("yo******")
                .contains("token_short=******");
    }

    private Map<String, String> getProperties(String key, String value) {
        final Map<String, String> properties = new LinkedHashMap<>();
        properties.put(key, value);
        return properties;
    }
}