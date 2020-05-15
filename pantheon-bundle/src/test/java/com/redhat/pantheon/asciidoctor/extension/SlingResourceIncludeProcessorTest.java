package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.api.SlingModels;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.PreprocessorReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.apache.jackrabbit.JcrConstants.JCR_DATA;
import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class SlingResourceIncludeProcessorTest {

    private final SlingContext slingContext = new SlingContext();

    @Test
    void resolveWithSymlinks() {
        //Given
        slingContext.build()
                .resource("/irrelevantResource")
                .resource("/realLocation/testFile",
                        "name", "dummy",
                        JCR_PRIMARYTYPE, "nt:file")
                .resource("/realLocation/testFile/jcr:content",
                        JCR_DATA, "some included content")
                .resource("/symlink",
                        "sling:resourceType", "pantheon/symlink",
                        "pant:target", "realLocation")
                .commit();

        Resource docResource = slingContext.resourceResolver().getResource("/irrelevantResource");

        Document doc = mock(Document.class);

        PreprocessorReader reader = mock(PreprocessorReader.class);
        when(reader.getFile()).thenReturn("");

        SlingModel model = mock(SlingModel.class);
        Field<String> jcrPrimaryType = mock(Field.class);

        when(jcrPrimaryType.get()).thenReturn("nt:file");
        when(model.field(JCR_PRIMARYTYPE, String.class)).thenReturn(jcrPrimaryType);
        when(model.adaptTo(FileResource.class)).thenReturn(SlingModels.getModel(
                slingContext.resourceResolver().getResource("/realLocation/testFile"), FileResource.class));

        slingContext.registerAdapter(Resource.class, SlingModel.class, model);

        //When
        SlingResourceIncludeProcessor proc = new SlingResourceIncludeProcessor(docResource);
        proc.process(doc, reader, "/symlink/testFile", null);

        //Then
        verify(reader).push_include(eq("some included content"), anyString(), anyString(), anyInt(), nullable(Map.class));
    }
}
