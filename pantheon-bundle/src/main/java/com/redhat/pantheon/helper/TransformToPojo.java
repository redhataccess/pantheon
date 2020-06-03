package com.redhat.pantheon.helper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;

/**
 * Helper methods to unmarshal JSON to corresponding POJO
 *
 * @author randalap
 */
public class TransformToPojo {

    /**
     * Marshal from json string to a specific POJO.
     *
     * @param <T>   the generic type
     * @param klass the klass
     * @param json  the json
     * @return the type
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("unchecked")
    public <T> T fromJson(final Class klass, final String json) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        return (T) mapper.readValue(new ByteArrayInputStream(json.getBytes()), klass);
    }

    /**
     * Unmarshal from JSON  in the reader to the class represnted by Class parameter .
     *
     * @param <T>    the type parameter
     * @param klass  the klass to which JSON has to be unmarshalled
     * @param reader the reader containing JSON
     * @return the Class instance containing data from JSON
     * @throws IOException the io exception
     */
    public <T> T fromJson(final Class klass, final Reader reader) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        return (T) mapper.readValue(reader, klass);
    }

    /**
     * Unmarshal from json in byte array format to the class represnted by Class parameter .
     *
     * @param <T>   the generic type
     * @param klass the klass
     * @param json  the json
     * @return the Class instance containing data from JSON
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("unchecked")
    public <T> T fromJson(final Class klass, final byte[] json) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return (T) mapper.readValue(new ByteArrayInputStream(json), klass);
    }
}
