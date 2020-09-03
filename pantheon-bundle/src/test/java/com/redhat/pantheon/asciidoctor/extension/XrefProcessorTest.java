package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.model.assembly.TableOfContents;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.util.TestUtils;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class XrefProcessorTest {

    private final SlingContext slingContext = new SlingContext();

//    @Test
//    void moduleToModule() {
//        //Given
//        slingContext.build()
//                .resource("/moduleA/en_US/variants/test-atts",
//                        "jcr:uuid", "abcd1234")
//                .resource("/moduleB",
//                        "sling:resourceType", "pantheon/module")
//                .resource("/moduleB/en_US/variants/test-atts",
//                        "jcr:uuid", "efgh5678")
//                .resource("/moduleC",
//                        "sling:resourceType", "pantheon/module")
//                .resource("/moduleC/en_US/variants/test-atts",
//                        "jcr:uuid", "ijkl9012")
//                .commit();
//        TestUtils.registerMockAdapter(DocumentVariant.class, slingContext);
//        TestUtils.registerMockAdapter(Document.class, slingContext);
//
//        DocumentVariant variant = slingContext.resourceResolver().getResource("/moduleA/en_US/variants/test-atts").adaptTo(DocumentVariant.class);
//
//        //When
//        XrefProcessor xp = new XrefProcessor(variant, new TableOfContents());
//        List<String> output = xp.preprocess(Arrays.asList(
//                "xref:moduleB[Link Label B1]",
//                "xref:moduleB#deep-target-b[Link Label B2]",
//                "<<moduleC,Link Label C1>>",
//                "<<moduleC#deep-target-c,Link Label C2>>"));
//
//        //Then
//        assertEquals("xref:efgh5678#[Link Label B1]", output.get(0));
//        assertEquals("xref:efgh5678#deep-target-b[Link Label B2]", output.get(1));
//        assertEquals("xref:ijkl9012#[Link Label C1]", output.get(2));
//        assertEquals("xref:ijkl9012#deep-target-c[Link Label C2]", output.get(3));
//    }
//
//    /**
//     * It is unusual for a module to xref to itself, but this is a good approximation for an inter-assembly xref
//     * which is easier to construct and test.
//     */
//    @Test
//    void moduleToSelf() {
//        //Given
//        slingContext.build()
//                .resource("/moduleB",
//                        "jcr:uuid", "abcd1234",
//                        "sling:resourceType", "pantheon/module")
//                .resource("/moduleB/en_US/variants/test-atts",
//                        "jcr:uuid", "efgh5678")
//                .commit();
//        TestUtils.registerMockAdapter(DocumentVariant.class, slingContext);
//        TestUtils.registerMockAdapter(ModuleVariant.class, slingContext);
//        TestUtils.registerMockAdapter(Document.class, slingContext);
//
//        ModuleVariant variant = slingContext.resourceResolver().getResource("/moduleB/en_US/variants/test-atts").adaptTo(ModuleVariant.class);
//        TableOfContents toc = new TableOfContents();
//        toc.addEntry(0, variant.getParentLocale().getParent());
//
//        //When
//        XrefProcessor xp = new XrefProcessor(variant, toc);
//        List<String> output = xp.preprocess(Arrays.asList(
//                "xref:moduleB[Link Label B1]",
//                "<<moduleB,Link Label B2>>"));
//
//        //Then
//        assertEquals("xref:#_abcd1234[Link Label B1]", output.get(0));
//        assertEquals("xref:#_abcd1234[Link Label B2]", output.get(1));
//    }
}
