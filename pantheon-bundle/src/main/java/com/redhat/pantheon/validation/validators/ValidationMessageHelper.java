package com.redhat.pantheon.validation.validators;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class ValidationMessageHelper {
    public static final String PROPERTIES_File = "validation-messages.properties";
    private Properties properties;
    private BundleContext bundleContext;
    final Logger log = LoggerFactory.getLogger(ValidationMessageHelper.class);
    public ValidationMessageHelper(BundleContext bundleContext) {
        properties = new Properties();
        this.bundleContext = bundleContext;
        loadProperties();
    }

    private void loadProperties() {
        try{
            properties.load(bundleContext.getBundle().getResource(PROPERTIES_File).openStream());
        } catch (IOException e) {
            getLog().error("Could not load validation message properties due  to", e);
        }
    }

    /**
     * Retrieve the validation message from the properties based on the key
     * @param key the key for the message
     * @return the message string corresponding to the key
     */
    public String getMessage(String key){
        return properties.getProperty(key);
    }

    public Logger getLog() {
        return log;
    }
}
