package no.cantara.config.test.tiger;

import no.cantara.config.ApplicationProperties;
import no.cantara.config.test.animal.AnimalFactory;

public class TigerFactory implements AnimalFactory {
    @Override
    public Class<?> providerClass() {
        return Tiger.class;
    }

    @Override
    public String alias() {
        return "bigT";
    }

    @Override
    public Tiger create(ApplicationProperties applicationProperties) {
        String say = applicationProperties.get("say", "roar");
        return new Tiger(say);
    }
}
