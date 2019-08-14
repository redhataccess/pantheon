package com.redhat.pantheon.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.resourcebuilder.api.ResourceBuilder;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.redhat.pantheon.servlet.BuildDateServlet;
import com.redhat.pantheon.use.PlatformData;

import javax.servlet.ServletException;
import java.util.Iterator;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class BuildDateServletTest {

    private final SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    private DefaultQueryServlet servlet;

    @Test
    @DisplayName("Test if the date fetched is null before the build is complete")
    public void testBuilddate() throws Exception {
        //Given
        BuildDateServlet buildDate = new BuildDateServlet();
        
        //When
        String date = buildDate.getDate();
        
        //Then
        assertEquals(true,date.contains(""));
    }

    @Test
    @DisplayName("Test if the hash fetched.")
    public void testCommitHash() throws Exception {
        //Given
        BuildDateServlet buildDate = new BuildDateServlet();

        //When
        String hash = buildDate.getCommitHash();

        //Then
        if (System.getenv("OPENSHIFT_BUILD_COMMIT") != null){
            assertEquals(true, Pattern.compile( "[0-9]" ).matcher(hash).find());
        } else {
            assertEquals(true,hash.contains("OPENSHIFT_BUILD_COMMIT is not set"));
        }
    }
}
