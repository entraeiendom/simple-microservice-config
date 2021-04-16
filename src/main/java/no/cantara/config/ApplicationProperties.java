package no.cantara.config;

import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The name of expected properties
 */
public class ApplicationProperties {

    public Set<String> getExpectedProperties(){
        final HashSet<String> names = new HashSet<>();
        names.add("base.url");
        return names;
    }
}

