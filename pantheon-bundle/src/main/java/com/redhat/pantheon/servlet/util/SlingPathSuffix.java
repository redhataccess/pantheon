package com.redhat.pantheon.servlet.util;

import com.google.common.collect.ImmutableMap;
import org.apache.sling.api.SlingHttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
 * Map<String, String> params = suffix.getParameters(request);
 * params.get("param1");
 * params.get("param2");
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
    // A list of the characters denoting the end of a resource path
    private static final String PATH_END_CHARS = escapeCharsForRegex('/', '?', '#');
    private final List<String> parameterNames = new ArrayList<>();
    private final Pattern pattern;

    /**
     * Constructs a new {@link SlingPathSuffix}
     * @param suffixTemplate The expected suffix template. See the class javadoc for the
     *                       specifics of this template string.
     */
    public SlingPathSuffix(final String suffixTemplate) {

        final Matcher matcher = PARAMETER_PATTERN.matcher(suffixTemplate);

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
        pattern = Pattern.compile(
                Pattern.quote(matcher.replaceAll("_____PARAM_____"))
                        .replace("_____PARAM_____", "\\E([^" + PATH_END_CHARS + "]*)\\Q")
                        // add this in case the url has extra 'things' (like parameters)
                        + ".*");
    }

    /**
     * Retrieves all suffix path parameters. This is a potentially expensive operation so
     * the result of this map should be saved for multiple uses.
     * @param request The specific {@link SlingHttpServletRequest} from which to extract the
     *                parameter.
     * @return An immutable map of parameters with the parameter name as index, and the value
     * of the parameter.
     */
    public Map<String, String> getParameters(final SlingHttpServletRequest request) {
        final ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
        final Matcher matcher = pattern.matcher(request.getRequestPathInfo().getSuffix());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Request path suffix does not match suffix expression");
        }
        for (int i = 1; i <= matcher.groupCount(); i++) {
            mapBuilder.put(parameterNames.get(i - 1), matcher.group(i));
        }

        return mapBuilder.build();
    }

    /**
     * Escapes each provided character individually and returns a string
     * with the escaped characters for use in regular expressions.
     */
    private static String escapeCharsForRegex(Character ... chars) {
        return Arrays.stream(chars)
                .map(character -> Pattern.quote(character.toString()))
                .collect(Collectors.joining());
    }

}
