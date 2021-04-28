package no.cantara.config;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;


public class ApplicationPropertiesDebugTest {

    @Test
    public void thatSourceOfSinglePropertyIsUnderstandableWhenDebugging() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("first.key", "first.value");
        map.put("second.key", "second.value");
        map.put("third.key", "third.value");
        ApplicationProperties instance = ApplicationProperties.builder()
                .property("first.property", "original.first.value")
                .property("third.key", "original.third.value")
                .map(map)
                .build();

        System.out.printf("%s%n", instance.sourcesOf("third.key"));
    }

    @Test
    public void thatDumpOfEntriesApplicationPropertiesIsUnderstandableWhenDebugging() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("first.key", "first.value");
        map.put("second.key", "second.value");
        map.put("third.key", "third.value");
        ApplicationProperties instance = ApplicationProperties.builder()
                .property("third.key", "original.third.value")
                .property("first.property", "original.first.value")
                .property("third.key", "Some in the middle value")
                .map(map)
                .build();

        System.out.printf("%n");
        System.out.printf("WITH%n");
        System.out.printf("%s%n", instance.debugAll(true));
        System.out.printf("%n");
        System.out.printf("%n");
        System.out.printf("WITHOUT%n");
        System.out.printf("%s%n", instance.debugAll(false));
    }
}