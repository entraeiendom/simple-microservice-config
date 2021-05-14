package no.cantara.config.test.duck;

import no.cantara.config.test.animal.Animal;

public class Duck implements Animal {
    private final String say;

    public Duck(String say) {
        this.say = say;
    }

    @Override
    public String say() {
        return say;
    }
}
