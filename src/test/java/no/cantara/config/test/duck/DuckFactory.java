package no.cantara.config.test.duck;

import no.cantara.config.ApplicationProperties;
import no.cantara.config.test.animal.AnimalFactory;

public class DuckFactory implements AnimalFactory {
    @Override
    public Class<?> providerClass() {
        return Duck.class;
    }

    @Override
    public String alias() {
        return "thaD";
    }

    @Override
    public Duck create(ApplicationProperties applicationProperties) {
        String say = applicationProperties.get("say", "quack");
        return new Duck(say);
    }
}
