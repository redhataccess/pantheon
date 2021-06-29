package com.redhat.pantheon.servlet.sitemap;

import com.redhat.pantheon.extension.url.CustomerPortalUrlUuidProvider;
import com.redhat.pantheon.extension.url.UrlException;
import com.redhat.pantheon.extension.url.UrlProvider;
import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.document.DocumentMetadata;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.model.document.DocumentVersion;
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
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
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

    private Set<Resource> getAsset(Resource resource, String documentVersionResourceType) {

        // Use Resource Filter Stream to limit memory consumption and path traversal
        ResourceFilterStream rfs = resource.adaptTo(ResourceFilterStream.class);

        return rfs.setChildSelector("[released/sling:resourceType] == '" + documentVersionResourceType + "'")
                        .stream()
                        .collect(Collectors.toSet());
    }


    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String documentVersionResourceType = "";
        Resource resource = request.getResourceResolver().getResource(RESOURCE_ROOT);

        if (request.getResource().getPath().startsWith("/api/sitemap/module.sitemap")) {
            documentVersionResourceType = RESOURCE_TYPE_MODULEVERSION;
        } else if (request.getResource().getPath().startsWith("/api/sitemap/assembly.sitemap")) {
            documentVersionResourceType = RESOURCE_TYPE_ASSEMBLYVERSION;
        } else {
            log.warn("[" + SiteMapServlet.class.getSimpleName() + "] unhandled resource type: " + resource.getClass());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Set<Resource> documentAssets = getAsset(resource, documentVersionResourceType);

        if(documentAssets == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(XML_MIME_TYPE);
        response.setCharacterEncoding(UTF_8);

        XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
        try {
            XMLStreamWriter stream = outputFactory.createXMLStreamWriter(response.getWriter());

            stream.writeStartDocument(XML_DOCUMENT_VERSION);
            stream.writeStartElement("", URL_SET, SITEMAP_NAMESPACE);
            stream.writeNamespace("", SITEMAP_NAMESPACE);
            Boolean isPortalUrl = System.getenv(PORTAL_URL) != null ? true : false;
            documentAssets.forEach(r -> {
                try {
                    writeXML(r, stream, request, isPortalUrl);
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
            });

            stream.writeEndElement();
            stream.writeEndDocument();

        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private void writeXML(Resource resource, XMLStreamWriter xmlStream, SlingHttpServletRequest slingRequest, Boolean isPortalUrl)
            throws XMLStreamException {
        xmlStream.writeStartElement(SITEMAP_NAMESPACE, URL);

        String locPath = resource.getPath();
        DocumentVariant documentVariant = null;
        Date dateModified = null;

        // Process external url
        if (isPortalUrl) {
            documentVariant = resource.adaptTo(DocumentVariant.class);

            if (documentVariant != null && documentVariant.released().isPresent()) {
                try {
                    locPath = new CustomerPortalUrlUuidProvider(documentVariant).generateUrlString();
                } catch (UrlException e) {
                    log.error("SiteMapServlet requested a URL for " + documentVariant.getPath() + " that could not be generated, this should never happen", e);
                }
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
}
