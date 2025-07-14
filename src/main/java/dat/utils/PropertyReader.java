package dat.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;

import dat.exceptions.DaoException;

public class PropertyReader {

    private static final String RESOURCE_NAME = "config.properties";
    private static final Boolean DEPLOYED = Boolean.parseBoolean(System.getenv("DEPLOYED"));

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PropertyReader.class);

    public static String getPropertyValue(String propName) {

        if(DEPLOYED) {

            return System.getenv(propName);

        }



        // REMEMBER TO BUILD WITH MAVEN FIRST. Read the property file if not deployed (else read system vars instead)
        // Read from ressources/config.properties or from pom.xml depending on the ressourceName
        try (InputStream inputStream = PropertyReader.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
            Properties props = new Properties();
            props.load(inputStream);

            String value = props.getProperty(propName);
            if (value != null) {
                return value.trim();  // Trim whitespace
            } else {
                throw new DaoException(String.format("Property %s not found in %s", propName, RESOURCE_NAME));
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            throw new DaoException(String.format("Could not read property %s. Did you remember to build the project with MAVEN?", propName));
        }
    }
}
