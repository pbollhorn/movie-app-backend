package dat.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;

public class PropertyReader {

    public static final Boolean DEPLOYED = Boolean.parseBoolean(System.getenv("DEPLOYED"));

    private static final String RESOURCE_NAME = "config.properties";
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PropertyReader.class);

    public static String getPropertyValue(String propName) {

        if (DEPLOYED) {

            String value = System.getenv(propName);
            if (value != null) {
                return value.trim();  // Trim whitespace
            } else {
                throw new IllegalArgumentException(String.format("Property %s not found in %s", propName, "environment variables"));
            }

        }

        try (InputStream inputStream = PropertyReader.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
            Properties props = new Properties();
            props.load(inputStream);

            String value = props.getProperty(propName);
            if (value != null) {
                return value.trim();  // Trim whitespace
            } else {
                throw new IllegalArgumentException(String.format("Property %s not found in %s", propName, RESOURCE_NAME));
            }
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Error reading property %s from %s$2. Does %s$2 exists?", propName, RESOURCE_NAME));
        }
    }
}
