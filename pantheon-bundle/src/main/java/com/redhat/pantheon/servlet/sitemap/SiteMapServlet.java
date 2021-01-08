package com.redhat.pantheon.servlet.sitemap;

import com.redhat.pantheon.extension.url.CustomerPortalUrlUuidProvider;
import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.assembly.AssemblyVariant;
import com.redhat.pantheon.model.document.DocumentMetadata;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.model.document.DocumentVersion;
import com.redhat.pantheon.model.module.ModuleVariant;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.resource.filter.ResourceFilterStream;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.query.Query;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.redhat.pantheon.helper.PantheonConstants.*;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Sitemap Servlet",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
        }
)
@SlingServletResourceTypes(
        resourceTypes = { SLING_SERVLET_DEFAULT },
        methods = "GET",
        extensions = SITEMAP_EXTENSION,
        selectors = SITE_MAP)
public class SiteMapServlet extends SlingAllMethodsServlet {
    private final Logger log = LoggerFactory.getLogger(SiteMapServlet.class);

    private static final String RESOURCE_ROOT = "/content/repositories";
    private static final String RESOURCE_TYPE_QUERY = "select parent.* from {type} as parent INNER JOIN {version} as child ON ISCHILDNODE(child, parent) WHERE ISDESCENDANTNODE(child,[" + RESOURCE_ROOT + "]) and name(child) = 'released' ORDER BY child.[jcr:lastModified] DESC";


    private List<Resource> getAsset(Resource resource, String documentVariantResourceType, String documentVersionResourceType) {
        log.info("[" + SiteMapServlet.class.getSimpleName() + "] resource type: " + documentVariantResourceType);
//        String primaryType = documentVariant.getResourceType();
        // Use Resource Filter Stream to limit memory consumption and path traversal
        ResourceFilterStream rfs = resource.adaptTo(ResourceFilterStream.class);

        return rfs.setBranchSelector("[jcr:primaryType] == '" + documentVariantResourceType + "'")
//                .setChildSelector("[jcr:primaryType] == '" + documentVersionResourceType + "'")
//                .setChildSelector("[released/jcr:primaryType] == $type")
//                .addParam("type", documentVersionResourceType)
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        String documentVariantResourceType = "";
        String documentVersionResourceType = "";
        Resource resource = request.getResourceResolver().getResource(RESOURCE_ROOT);

        if (request.getResource().getPath().startsWith("/api/module.sitemap")) {
            documentVariantResourceType = "pant:moduleVariant";
            documentVersionResourceType = "pant:moduleVersion";
        } else if (request.getResource().getPath().startsWith("/api/assembly.sitemap")) {
            documentVariantResourceType = "pant:assemblyVariant";
            documentVersionResourceType = "pant:assemblyVersion";
        } else {
            log.warn("[" + SiteMapServlet.class.getSimpleName() + "] unhandled resource type: " + resource.getClass());
        }

//        List<Resource> documentAssets = getAsset(resource, documentVariantResourceType, documentVersionResourceType);
        // get assets through traversal
//        List<Resource> documentAssets = getAssetsThroughTraversal(resource, documentVariantResourceType, documentVariantResourceType);
//        List<Resource> documentAssets = getAssetsThroughTraversal2(resource, documentVariantResourceType, documentVariantResourceType);
        List<Resource> documentAssets = getAssetsThroughQuery(resource, documentVariantResourceType, documentVersionResourceType);
        if(documentAssets == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(PantheonConstants.XML_MIME_TYPE);
        response.setCharacterEncoding(PantheonConstants.UTF_8);

        XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
        try {
            XMLStreamWriter stream = outputFactory.createXMLStreamWriter(response.getWriter());

            stream.writeStartDocument(XML_DOCUMENT_VERSION);
            stream.writeStartElement("", URL_SET, SITEMAP_NAMESPACE);
            stream.writeNamespace("", SITEMAP_NAMESPACE);

            documentAssets.forEach(r -> {
                log.info("[" + SiteMapServlet.class.getSimpleName() + "] documentAssets r: " + r.getPath());
            try {
                writeXML(r, stream, request);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
            });

//            for (Resource child : resource.getChildren()) {
//                if (documentVariantResourceType.equals(child.getResourceType())) {
//                    Resource documentVersion = child.getChild("released");
//                    if (documentVersion != null && documentVersionResourceType.equals(documentVersion.getResourceType())) {
//                        try {
//                            writeXML(child, stream, request);
//                        } catch (XMLStreamException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
            stream.writeEndElement();
            stream.writeEndDocument();

        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }
    private void writeXML(Resource resource, XMLStreamWriter xmlStream, SlingHttpServletRequest slingRequest)
            throws XMLStreamException {
        xmlStream.writeStartElement(SITEMAP_NAMESPACE, URL);

//        String protocolPort = "http";
//        if (slingRequest.isSecure())
//            protocolPort = "https";

        String locPath = resource.getPath();
        DocumentVariant documentVariant = null;
        Date dateModified = null;

        // Process external url
        if (System.getenv(PORTAL_URL) != null) {
            if (resource.isResourceType(RESOURCE_TYPE_ASSEMBLYVARIANT)) {
                documentVariant = resource.adaptTo(AssemblyVariant.class);
            } else if (resource.isResourceType(RESOURCE_TYPE_MODULEVARIANT)) {
                documentVariant = resource.adaptTo(ModuleVariant.class);
            }
            if (documentVariant != null && documentVariant.released().isPresent()) {
                locPath = new CustomerPortalUrlUuidProvider().generateUrlString(documentVariant);
            }
        } else {
            locPath = resource.getPath();
        }
        writeXMLElement(xmlStream, LOC, locPath);
        if (documentVariant != null) {
            Optional<DocumentMetadata> releasedMetadata = (Optional<DocumentMetadata>) Child.from(documentVariant)
                    .toChild(DocumentVariant::released)
                    .toChild(DocumentVersion::metadata)
                    .asOptional();
            dateModified = new Date(releasedMetadata.get().getValueMap().containsKey("pant:datePublished") ? releasedMetadata.get().datePublished().get().getTimeInMillis() : resource.getResourceMetadata().getModificationTime());
        } else {
            dateModified = new Date(resource.getResourceMetadata().getModificationTime());
        }

        if (dateModified != null) {
            writeXMLElement(xmlStream, LAST_MOD, dateModified.toInstant().toString());
        }

        xmlStream.writeEndElement();
    }

    private void writeXMLElement(final XMLStreamWriter xmlStream, final String elementName, final String xmlText)
            throws XMLStreamException {
        xmlStream.writeStartElement(SITEMAP_NAMESPACE, elementName);
        xmlStream.writeCharacters(xmlText);
        xmlStream.writeEndElement();
    }

    private List<Resource> getAssetsThroughTraversal(Resource resource, String documentVariantResourceType, String documentVersionResourceType) {
        List<Resource> assets = new ArrayList<Resource>();
        for (Resource child : resource.getChildren()) {
            log.info("[" + SiteMapServlet.class.getSimpleName() + "] child : " + child.getPath());
            log.info("[" + SiteMapServlet.class.getSimpleName() + "] child ResourceType : " + child.getResourceType());

            if (documentVariantResourceType.equals(child.getResourceType())) {
                log.info("[" + SiteMapServlet.class.getSimpleName() + "] child ResourceType : " + child.getResourceType());
                Resource documentVersion = child.getChild("released");
                if (documentVersion != null && documentVersionResourceType.equals(documentVersion.getResourceType())) {
                    assets.add(child);
                    if (child.getChildren() != null) {

                        assets.addAll(getAssetsThroughTraversal(child, documentVariantResourceType, documentVersionResourceType));
                    }

                }
            }
        }
        return assets;
    }

    private List<Resource> getAssetsThroughTraversal2(Resource resource, String documentVariantResourceType, String documentVersionResourceType) {
        List<Resource> assets = new ArrayList<Resource>();
        Iterator<Resource> it = resource.listChildren();
        while (it.hasNext()) {
            if (!it.next().getResourceType().isEmpty()) {
            log.info("[" + SiteMapServlet.class.getSimpleName() + "] it.next().getResourceType : " + it.next().getResourceType());
            if (documentVariantResourceType.equals(it.next().getResourceType())) {
                assets.add(it.next());
            }
        }
        }

        return assets;
    }

    private List<Resource> getAssetsThroughQuery(Resource resource, String documentVariantResourceType, String documentVersionResourceType) {
        List<Resource> assets = new ArrayList<Resource>();
        String queryStrType = RESOURCE_TYPE_QUERY.replace("{type}","[" + documentVariantResourceType + "]");
        String queryStrVersion = queryStrType.replace("{version}", "[" + documentVersionResourceType + "]");
        Iterator<Resource> typedResourceIterator = resource.getResourceResolver().findResources(queryStrVersion, Query.JCR_SQL2);

        typedResourceIterator.forEachRemaining(r -> {
            assets.add(r);
        });
        return assets;
    }

//    public static Set<Resource> getNestedComponentTypes(Resource root, boolean inclusive) {
//
//        Set<Resource> flattenedResourceTree = flattenResourceTree(root, inclusive);
//
//        Set<string> resourceTypeSet = Sets.newHashSet();
//
//        for (Resource currentResource : flattenedResourceTree) {
//            if (currentResource.getResourceType() != null && StringUtils.isNotBlank(currentResource.getResourceType())) {
//                resourceTypeSet.add(currentResource.getResourceType());
//            }
//        }
//
//        return resourceTypeSet;
//
//    }
}
