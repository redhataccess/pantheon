package com.redhat.pantheon.servlet.util;

import com.google.common.hash.HashCode;
import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.jcr.JcrResources;
import com.redhat.pantheon.model.HashableFileResource;
import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentLocale;
import com.redhat.pantheon.model.document.DocumentMetadata;
import com.redhat.pantheon.servlet.ServletUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.servlets.post.PostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class VersionUploadHelper {
    private static final Logger log = LoggerFactory.getLogger(VersionUploadHelper.class);

    private static final Set<String> METADATA_COPY_EXCLUDES = Collections.unmodifiableSet(
            new HashSet<>(
                    Arrays.asList(
                            "jcr:description",
                            "jcr:lastModified",
                            "jcr:primaryType",
                            "jcr:title",
                            "pant:dateUploaded",
                            "pant:datePublished"
                    )));

    public static void doRun(SlingHttpServletRequest request,
                            PostResponse response,
                            AsciidoctorService asciidoctorService,
                            Class<? extends Document> doctype,
                            BiConsumer<Document, DocumentMetadata> doctypeExtras) throws IOException {
        String locale = ServletUtils.paramValue(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE.toString());
        String path = request.getResource().getPath();

        log.debug("Pushing new document version at: " + path + " with locale: " + locale);
        int responseCode = HttpServletResponse.SC_OK;

        // Try to find the document
        ResourceResolver resolver = request.getResourceResolver();
        Resource resource = resolver.getResource(path);
        Document document;
        if(resource == null) {
            document =
                    SlingModels.createModel(
                            resolver,
                            path,
                            doctype);
            responseCode = HttpServletResponse.SC_CREATED;
        } else {
            document = resource.adaptTo(doctype);
        }

        Locale localeObj = LocaleUtils.toLocale(locale);
        DocumentLocale documentLocale = document.locale(localeObj).getOrCreate();
        HashableFileResource draftSrc = documentLocale
                .source().getOrCreate()
                .draft().getOrCreate();

        // Check if the content is the same as what is hashed already
        HashCode incomingSrcHash =
                ServletUtils.handleParamAsStream(request, "asciidoc",
                        inputStream -> {
                            try {
                                return JcrResources.hash(inputStream);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
        String storedSrcHash = draftSrc.hash().get();
        // If the source content is the same, don't update it
        if (incomingSrcHash.toString().equals(storedSrcHash)) {
            responseCode = HttpServletResponse.SC_NOT_MODIFIED;
        } else {
            ServletUtils.handleParamAsStream(request, "asciidoc",
                    inputStream -> {
                        Session session = resolver.adaptTo(Session.class);
                        draftSrc.jcrContent().getOrCreate()
                                .jcrData().toFieldType(InputStream.class)
                                .set(inputStream);
                        return null;
                    });
            draftSrc.hash().set( incomingSrcHash.toString() );
            draftSrc.jcrContent().getOrCreate()
                    .mimeType().set("text/x-asciidoc");

            String variantName = documentLocale.getWorkspace().getCanonicalVariantName();
            Child metadataChild = documentLocale
                    .variants().getOrCreate()
                    .variant(variantName)
                    .getOrCreate()
                    .draft().getOrCreate()
                    .metadata();
            DocumentMetadata draftMetadata = (DocumentMetadata) Optional.ofNullable(metadataChild.get()).orElseGet(() -> {
                DocumentMetadata draftMeta = (DocumentMetadata) metadataChild.create();
                copyMetadataFromReleased(draftMeta, document, localeObj, variantName);
                return draftMeta;
            });
            draftMetadata.dateModified().set(Calendar.getInstance());

            resolver.commit();

            Map<String, Object> context = asciidoctorService.buildContextFromRequest(request);
            asciidoctorService.getDocumentHtml(document, localeObj, document.getWorkspace().getCanonicalVariantName(),
                    true, context, true);

            DocumentMetadata documentMetadata = documentLocale
                    .variants().getOrCreate()
                    .variant(
                            documentLocale.getWorkspace().getCanonicalVariantName())
                    .getOrCreate()
                    .draft().getOrCreate()
                    .metadata().getOrCreate();
            documentMetadata.dateModified().set(Calendar.getInstance());

            Optional.ofNullable(doctypeExtras).ifPresent(extras -> extras.accept(document, documentMetadata));
        }

        // TODO: trigger an event to generate the html asynchronous
        resolver.commit();
        response.setStatus(responseCode, "");
    }

    private static void copyMetadataFromReleased(DocumentMetadata draftMetadata,
                                                Document document,
                                                Locale locale,
                                                String variant) {
        ModifiableValueMap draftMap = draftMetadata.adaptTo(ModifiableValueMap.class);

        // Need to copy metadata from released onto draft
        document.getReleasedMetadata(locale, variant).ifPresent(releasedMetadata ->
                releasedMetadata.getValueMap().entrySet().stream()
                        .filter(entry -> !METADATA_COPY_EXCLUDES.contains(entry.getKey()))
                        .forEach(entry -> draftMap.put(entry.getKey(), entry.getValue())));
    }
}
