package com.redhat.pantheon.model;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.servlet.AssemblyRendering;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.models.annotations.Default;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.inject.Named;
import java.io.IOException;
import java.util.Calendar;

/**
 * Represents rendering as an interface to be implemented.
 *
 */

public interface Rendering {
    public  void getRenderedHTML(SlingHttpServletRequest request, SlingHttpServletResponse resposne) throws IOException;
}
