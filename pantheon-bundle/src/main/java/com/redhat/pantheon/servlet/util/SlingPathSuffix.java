package com.redhat.pantheon.servlet.util;

import org.apache.sling.api.SlingHttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the suffix of a sling url and allows to extract information from it.
 * To be used primarily in servlets where suffix information contains input information
 * (i.e. parameters).
 * <br><br>
 *
 * Parameters may be extracted like so:
 *
 * <pre>{@code
 * SlingPathSuffix suffix = new SlingPathSuffix("/path/with/{param1}/and/{param2}");
 * suffix.getParam("param1", request);
 * suffix.getParam("param2", request);
 * }</pre>
 *
 * The suffix needs a template or pattern to be provided in order to be able to extract
 * information from it. It may be a completely static pattern or contain placeholders for
 * named parameters.
 *
 * @author Carlos Munoz
 */
public class SlingPathSuffix {

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("(\\{[a-zA-Z0-9]+})");
    private final List<String> parameterNames = new ArrayList<>();
    private Map<String, String> parameterMap;
    private final Pattern pattern;

    public SlingPathSuffix(final String parameterTemplate) {

        final Matcher matcher = PARAMETER_PATTERN.matcher(parameterTemplate);

        while (matcher.find()) {
            if (matcher.groupCount() == 1) {
                final String group = matcher.group(1);
                if (group.length() > 2) {
                    parameterNames.add(group.substring(1, group.length() - 1));
                } else {
                    parameterNames.add(group);
                }
            }
        }
        pattern = Pattern.compile(Pattern.quote(matcher.replaceAll("_____PARAM_____")).replace("_____PARAM_____", "\\E([^/]*)\\Q"));

    }

    public String getParam(final String name, SlingHttpServletRequest request) {
        return parametersByName(request.getRequestPathInfo().getSuffix()).get(name);
    }

    private Map<String, String> parametersByName(final String uriString) {
        if(parameterMap == null) {
            parameterMap = new HashMap<>();
            final Matcher matcher = pattern.matcher(uriString);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Uri does not match");
            }
            for (int i = 1; i <= matcher.groupCount(); i++) {
                parameterMap.put(parameterNames.get(i - 1), matcher.group(i));
            }
        }
        return parameterMap;
    }
}
