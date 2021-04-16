package no.cantara.config;

import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Read environment variables from classpath, local config and environment.
 */
public class ServiceConfig {
    private static final Logger log = getLogger(ServiceConfig.class);

    public static final String DEFAULT_PROPERTIES_FILE_NAME = "default.properties";
    // Other sources and what is a good ordering?
    // Add application.properties
    // Add application.yaml
    // Add microprofile_config.properties
    // Environment properties
    public static final String LOCAL_CONFIG_FILE_NAME = "local_config.properties";

    public static Properties loadProperties() {
        return loadProperties(DEFAULT_PROPERTIES_FILE_NAME);
    }
    static Properties loadProperties(String defaultPropertiesFileName) {
        // create and load default properties
        Properties defaultProps = new Properties();

        try {
            InputStream in = ServiceConfig.class.getClassLoader().getResourceAsStream(defaultPropertiesFileName);
            defaultProps.load(in);
            in.close();
            log.debug("Loaded {} default properties from {}", defaultProps.size(), defaultPropertiesFileName);
        } catch (FileNotFoundException e) {
            log.debug("{} was not found.", defaultPropertiesFileName);
        } catch (IOException e) {
            log.debug("Failed to read {}. Reason {}", defaultPropertiesFileName, e.getMessage());
        }

        Properties applicationProps = new Properties();
        for (Object key : defaultProps.keySet()) {
            applicationProps.put(key, defaultProps.getProperty((String) key));
        }

        InputStream in = null;
        try {
            in = new FileInputStream(LOCAL_CONFIG_FILE_NAME);
        } catch (FileNotFoundException e) {
            log.debug("{} was not found.", LOCAL_CONFIG_FILE_NAME);
            in = ServiceConfig.class.getClassLoader().getResourceAsStream(LOCAL_CONFIG_FILE_NAME);
        } catch (IOException e) {
            log.debug("Failed to read {}. Reason {}", LOCAL_CONFIG_FILE_NAME, e.getMessage());
        }
        if (in != null) {
            try {
                applicationProps.load(in);
                in.close();
            } catch (IOException ioe) {
                log.debug("Failed to read {}. Reason {}", LOCAL_CONFIG_FILE_NAME, ioe.getMessage());
            }
        }

        return applicationProps;
    }

    @Deprecated
    public static String getProperty(String camel_case_key) {
        String value = System.getenv(camel_case_key);
        if (value == null || value.isEmpty()) {
            Properties properties = loadProperties();
            value = properties.getProperty(camel_case_key);
        }
        return value;
    }
}

