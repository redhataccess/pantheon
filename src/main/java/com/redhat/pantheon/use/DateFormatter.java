package com.redhat.pantheon.use;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ben on 4/1/19.
 * http://blogs.adobe.com/experiencedelivers/experience-management/htl-date-formatting/
 * https://sling.apache.org/documentation/bundles/models.html#basic-usage
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class DateFormatter {

    public String value;

    @Inject
    public DateFormatter(@Named("format") String format, @Named("date") Calendar date) {
        value = new SimpleDateFormat(format).format(date.getTime());
    }
}