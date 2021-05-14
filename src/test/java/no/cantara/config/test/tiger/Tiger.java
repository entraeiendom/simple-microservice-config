package no.cantara.config.test.tiger;

import no.cantara.config.test.animal.Animal;

public class Tiger implements Animal {
    private final String say;

    public Tiger(String say) {
        this.say = say;
    }

    @Override
    public String say() {
        return say;
    }
}
