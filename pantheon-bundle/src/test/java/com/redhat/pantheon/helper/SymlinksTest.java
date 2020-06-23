package com.redhat.pantheon.helper;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class SymlinksTest {


    SlingContext sc = new SlingContext();


    @Test
    void symlink() {
        // Given
        String imagePath = "/path/to/my";
        String imageName = "/image.png";
        String symlinkPath = "/something/else";
        String symlinkName = "/symlink";

        sc.build()
                .resource(imagePath + imageName)
                .resource(symlinkPath + symlinkName,
                        "sling:resourceType", "pantheon/symlink",
                        "pant:target", "..///../" + imagePath) // <-- triple slash and trailing slash are important,
                // tests ability to ignore extra slashes just like local fs does
                .commit();

        // When
        Resource res = Symlinks.resolve(sc.resourceResolver(), symlinkPath + symlinkName + imageName);
        Resource res2 = Symlinks.resolve(sc.resourceResolver(), "garbage");

        // Then
        assertNotNull(res);
        assertNotEquals("sling:nonexisting", res.getResourceType());
        assertEquals("sling:nonexisting", res2.getResourceType());
    }
}
