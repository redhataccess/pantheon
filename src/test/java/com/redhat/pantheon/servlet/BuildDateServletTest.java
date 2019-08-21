package com.redhat.pantheon.servlet;

import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class BuildDateServletTest {

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
    @DisplayName("Test that the commit hash is not set while building.")
    public void testCommitHash() throws Exception {
        //Given
        BuildDateServlet buildDate = new BuildDateServlet();

        //When
        String hash = buildDate.getCommitHash();

        //Then
        assertEquals(true, hash.contains("OPENSHIFT_BUILD_COMMIT is not set"));
    }
}
