package com.redhat.pantheon.sling;

import org.apache.sling.commons.log.logback.ConfigProvider;
import org.osgi.service.component.annotations.Component;
import org.xml.sax.InputSource;

/**
 * Created by ben on 3/29/19.
 */

@Component(
        service = ConfigProvider.class
)
public class LogbackConfigProvider implements ConfigProvider {

    @Override
    public InputSource getConfigSource() {
        // https://sling.apache.org/documentation/development/logging.html
        return new InputSource(getClass().getClassLoader().getResourceAsStream("logback.xml"));
    }
}
