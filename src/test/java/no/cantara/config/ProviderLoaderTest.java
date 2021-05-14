package no.cantara.config;

import no.cantara.config.test.animal.Animal;
import no.cantara.config.test.animal.AnimalFactory;
import no.cantara.config.test.duck.Duck;
import no.cantara.config.test.tiger.Tiger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ProviderLoaderTest {

    @Test
    public void tigerFactoryOfWithDefaultConfig() {
        ApplicationProperties applicationProperties = ApplicationProperties.builder().build();
        AnimalFactory animalFactory = ProviderLoader.factoryOf("no.cantara.config.test.tiger.Tiger", AnimalFactory.class);
        Animal animal = animalFactory.create(applicationProperties.subTree("animal.tiger"));
        assertEquals(Tiger.class, animal.getClass());
        assertEquals("roar", animal.say());
        Animal animal2 = animalFactory.create(applicationProperties.subTree("animal.tiger"));
        assertEquals(Tiger.class, animal2.getClass());
        assertEquals("roar", animal2.say());
        assertNotEquals(animal, animal2);
    }

    @Test
    public void tigerConfigurator() {
        ApplicationProperties applicationProperties = ApplicationProperties.builder()
                .property("animal.tiger.say", "Rooooooaaaaaarrr")
                .build();
        Animal animal = ProviderLoader.configure(applicationProperties.subTree("animal.tiger"), "no.cantara.config.test.tiger.Tiger", AnimalFactory.class);
        assertEquals(Tiger.class, animal.getClass());
        assertEquals("Rooooooaaaaaarrr", animal.say());
        Animal animal2 = ProviderLoader.configure(applicationProperties.subTree("animal.tiger"), "Tiger", AnimalFactory.class);
        assertEquals(Tiger.class, animal2.getClass());
        assertEquals("Rooooooaaaaaarrr", animal2.say());
        Animal animal3 = ProviderLoader.configure(applicationProperties.subTree("animal.tiger"), "bigT", AnimalFactory.class);
        assertEquals(Tiger.class, animal3.getClass());
        assertEquals("Rooooooaaaaaarrr", animal3.say());
    }

    @Test
    public void duckConfigurator() {
        ApplicationProperties applicationProperties = ApplicationProperties.builder()
                .property("animal.duck.say", "Quack quack quack")
                .build();
        Animal animal = ProviderLoader.configure(applicationProperties.subTree("animal.duck"), "no.cantara.config.test.duck.Duck", AnimalFactory.class);
        assertEquals(Duck.class, animal.getClass());
        assertEquals("Quack quack quack", animal.say());
        Animal animal2 = ProviderLoader.configure(applicationProperties.subTree("animal.duck"), "Duck", AnimalFactory.class);
        assertEquals(Duck.class, animal2.getClass());
        assertEquals("Quack quack quack", animal2.say());
        Animal animal3 = ProviderLoader.configure(applicationProperties.subTree("animal.duck"), "thaD", AnimalFactory.class);
        assertEquals(Duck.class, animal3.getClass());
        assertEquals("Quack quack quack", animal3.say());
    }
}
