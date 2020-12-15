package com.redhat.pantheon.servlet.util;

import com.google.common.collect.ImmutableMap;
import com.redhat.pantheon.model.assembly.AssemblyVariant;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.servlet.assembly.AssemblyVariantJsonServlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.jcr.RepositoryException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Lisa Davidson
 */
@ExtendWith({SlingContextExtension.class})
public class ServletHelperTest {
    SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);
    String testHTML = "<!DOCTYPE html> <html lang=\"en\"> <head><title>test title</title></head> <body " +
            "class=\"article\"><h1>test title</h1> </header> </body> </html>";
    @Test
    void getResourceByUuidTest() throws RepositoryException {
        // Given
        slingContext.build()
                .resource("/content/repositories/repo/module",
                        "jcr:primaryType", "pant:module")
                .commit();

        registerMockAdapter(Module.class, slingContext);
        ServletHelper servletHelper = new ServletHelper();
        MockSlingHttpServletRequest request = slingContext.request();
        String resourceUuid = slingContext.resourceResolver()
                .getResource("/content/repositories/repo/module")
                .getValueMap()
                .get("jcr:uuid")
                .toString();
        // When
        Resource resource = servletHelper.getResourceByUuid(request, resourceUuid);
        // Then
        assertEquals("pantheon/module", resource.getResourceType());
        assertEquals("pant:module", resource.getValueMap().get("jcr:primaryType"));
    }

    @Test
    void setAssemblyDetailsTest() throws RepositoryException {
        slingContext.build()
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT",
                        "jcr:primaryType", "pant:assemblyVariant")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released",
                        "jcr:primaryType", "pant:assemblyVersion")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/content/0",
                        "jcr:moduleVariantUuid", "1234-5678-9012")
                .commit();

        registerMockAdapter(AssemblyVariant.class, slingContext);
        AssemblyVariantJsonServlet servlet = new AssemblyVariantJsonServlet();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT") );


        List<HashMap<String, String>> includeAssemblies =new ArrayList<>();
        ServletHelper.setAssemblyData(slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT")
                , includeAssemblies,false, false);
        assertFalse(includeAssemblies.isEmpty(),"assembly details should not be empty ");
    }
    @Test
    void setAssemblyDetailsTestWithPath() throws RepositoryException {
        slingContext.build()
                .resource("/content/repositories/rhel-8-docs",
                        "jcr:primaryType", "pant:workspace")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes",
                        "jcr:primaryType", "pant:assembly")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT",
                        "jcr:primaryType", "pant:assemblyVariant")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released",
                        "jcr:primaryType", "pant:assemblyVersion")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/content/0",
                        "jcr:moduleVariantUuid", "1234-5678-9012")
                .commit();

        registerMockAdapter(AssemblyVariant.class, slingContext);
        AssemblyVariantJsonServlet servlet = new AssemblyVariantJsonServlet();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT") );


        List<HashMap<String, String>> includeAssemblies =new ArrayList<>();
        ServletHelper.setAssemblyData(slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT")
                , includeAssemblies,true, false);
        assertFalse(includeAssemblies.isEmpty(),"assembly details should not be empty ");
        assertTrue(includeAssemblies.get(0).containsKey("path"));
    }
    @Test
    void setAssemblyDetailsTestWithDraftAndReleaseAndCantAddDraft() throws RepositoryException {
        slingContext.build()
                .resource("/content/repositories/rhel-8-docs",
                        "jcr:primaryType", "pant:workspace")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes",
                        "jcr:primaryType", "pant:assembly")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT",
                        "jcr:primaryType", "pant:assemblyVariant")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released",
                        "jcr:primaryType", "pant:assemblyVersion")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/content/0",
                        "jcr:moduleVariantUuid", "1234-5678-9012")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft",
                        "jcr:primaryType", "pant:assemblyVersion")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/metadata",
                        "jcr:title", "A new title",
                        "jcr:description", "A description")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/content/0",
                        "jcr:moduleVariantUuid", "1234-5678-9022")
                .commit();

        registerMockAdapter(AssemblyVariant.class, slingContext);

        AssemblyVariantJsonServlet servlet = new AssemblyVariantJsonServlet();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT") );


        List<HashMap<String, String>> includeAssemblies =new ArrayList<>();
        ServletHelper.setAssemblyData(slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT")
                , includeAssemblies,true, false);
        assertFalse(includeAssemblies.isEmpty(),"assembly details should not be empty ");
        assertTrue(includeAssemblies.get(0).containsKey("path"));
        assertEquals("A title", includeAssemblies.get(0).get("title"));
    }

    @Test
    void setAssemblyDetailsTestWithPathAndDraft() throws RepositoryException {
        slingContext.build()
                .resource("/content/repositories/rhel-8-docs",
                        "jcr:primaryType", "pant:workspace")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes",
                        "jcr:primaryType", "pant:assembly")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT",
                        "jcr:primaryType", "pant:assemblyVariant")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft",
                        "jcr:primaryType", "pant:assemblyVersion")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/content/0",
                        "jcr:moduleVariantUuid", "1234-5678-9012")
                .commit();

        registerMockAdapter(AssemblyVariant.class, slingContext);
        AssemblyVariantJsonServlet servlet = new AssemblyVariantJsonServlet();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT") );


        List<HashMap<String, String>> includeAssemblies =new ArrayList<>();
        ServletHelper.setAssemblyData(slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT")
                , includeAssemblies,true, true);
        assertFalse(includeAssemblies.isEmpty(),"assembly details should not be empty ");
        assertTrue(includeAssemblies.get(0).containsKey("path"));
    }

    @Test
    void setAssemblyDetailsTestWithoutPathAndDraft() throws RepositoryException {
        slingContext.build()
                .resource("/content/repositories/rhel-8-docs",
                        "jcr:primaryType", "pant:workspace")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes",
                        "jcr:primaryType", "pant:assembly")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT",
                        "jcr:primaryType", "pant:assemblyVariant")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft",
                        "jcr:primaryType", "pant:assemblyVersion")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/content/0",
                        "jcr:moduleVariantUuid", "1234-5678-9012")
                .commit();

        registerMockAdapter(AssemblyVariant.class, slingContext);
        AssemblyVariantJsonServlet servlet = new AssemblyVariantJsonServlet();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT") );


        List<HashMap<String, String>> includeAssemblies =new ArrayList<>();
        ServletHelper.setAssemblyData(slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT")
                , includeAssemblies,false, true);
        assertFalse(includeAssemblies.isEmpty(),"assembly details should not be empty ");
        assertFalse(includeAssemblies.get(0).containsKey("path"));
    }

    @Test
    void setAssemblyDetailsTestDraftAndCannotHaveDraft() throws RepositoryException {
        slingContext.build()
                .resource("/content/repositories/rhel-8-docs",
                        "jcr:primaryType", "pant:workspace")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes",
                        "jcr:primaryType", "pant:assembly")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT",
                        "jcr:primaryType", "pant:assemblyVariant")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft",
                        "jcr:primaryType", "pant:assemblyVersion")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/content/0",
                        "jcr:moduleVariantUuid", "1234-5678-9012")
                .commit();

        registerMockAdapter(AssemblyVariant.class, slingContext);
        AssemblyVariantJsonServlet servlet = new AssemblyVariantJsonServlet();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT") );


        List<HashMap<String, String>> includeAssemblies =new ArrayList<>();
        ServletHelper.setAssemblyData(slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT")
                , includeAssemblies,false, false);
        assertTrue(includeAssemblies.isEmpty(),"assembly details should  be empty ");
    }
    @Test
    public void getModuleUUIDFromVariant(){
        // Given
        slingContext.build()
                .resource("/content/repositories/repo",
                        "jcr:primaryType", "pant:workspace")
                .resource("/content/repositories/repo/module","jcr:primaryType", "pant:moduleVersion")
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT",
                        "jcr:primaryType", "pant:moduleVariant")
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/released/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description")
                .resource("/content/repositories/repo/module/en_US/source/released/jcr:content",
                        "jcr:data", "This is the source content")
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/released/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .commit();

        registerMockAdapter(ModuleVariant.class, slingContext);
        String uuid=  ServletHelper.getModuleUuidFromVariant(slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/variants/DEFAULT").adaptTo(ModuleVariant.class));
        assertNotNull(uuid, "module uuid should not be null");
    }
}
