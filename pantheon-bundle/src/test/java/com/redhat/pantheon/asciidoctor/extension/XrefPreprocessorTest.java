package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.assembly.TableOfContents;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.util.TestUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.asciidoctor.extension.PreprocessorReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.jackrabbit.JcrConstants.JCR_DATA;
import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class XrefPreprocessorTest {

    private final SlingContext slingContext = new SlingContext();

    @Test
    void moduleToModule() {
        //Given
        slingContext.build()
                .resource("/moduleA/en_US/variants/test-atts",
                        "jcr:uuid", "abcd1234")
                .resource("/moduleB",
                        "sling:resourceType", "pantheon/module")
                .resource("/moduleB/en_US/variants/test-atts",
                        "jcr:uuid", "efgh5678")
                .commit();
        TestUtils.registerMockAdapter(DocumentVariant.class, slingContext);
        TestUtils.registerMockAdapter(Document.class, slingContext);

        DocumentVariant variant = slingContext.resourceResolver().getResource("/moduleA/en_US/variants/test-atts").adaptTo(DocumentVariant.class);

        //When
        XrefPreprocessor xp = new XrefPreprocessor(variant, new TableOfContents());
        List<String> output = xp.preprocess(Arrays.asList("xref:moduleB[Link Label]"));
        System.out.println(output.get(0));

        //Then
        assertEquals("xref:efgh5678#[Link Label]", output.get(0));
    }

    /**
     * It is unusual for a module to xref to itself, but this is a good approximation for an inter-assembly xref
     * which is easier to construct and test.
     */
    @Test
    void moduleToSelf() {
        //Given
        slingContext.build()
                .resource("/moduleB",
                        "jcr:uuid", "abcd1234",
                        "sling:resourceType", "pantheon/module")
                .resource("/moduleB/en_US/variants/test-atts",
                        "jcr:uuid", "efgh5678")
                .commit();
        TestUtils.registerMockAdapter(DocumentVariant.class, slingContext);
        TestUtils.registerMockAdapter(ModuleVariant.class, slingContext);
        TestUtils.registerMockAdapter(Document.class, slingContext);

        ModuleVariant variant = slingContext.resourceResolver().getResource("/moduleB/en_US/variants/test-atts").adaptTo(ModuleVariant.class);
        TableOfContents toc = new TableOfContents();
        toc.addEntry(null, variant.getParentLocale().getParent());

        //When
        XrefPreprocessor xp = new XrefPreprocessor(variant, toc);
        List<String> output = xp.preprocess(Arrays.asList("xref:moduleB[Link Label]"));
        System.out.println(output.get(0));

        //Then
        assertEquals("xref:#_abcd1234[Link Label]", output.get(0));
    }
}
