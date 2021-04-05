package com.redhat.pantheon.servlet;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.jcr.JcrQueryHelper;
import com.redhat.pantheon.model.HashableFileResource;
import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.module.*;
import com.redhat.pantheon.validation.model.Validations;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.servlet.Servlet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static java.util.stream.Collectors.toList;

/**
 * Created by ben on 4/18/19.
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet which provides initial module listing and search functionality",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/pantheon/internal/modules.json")
public class ModuleListingServlet extends AbstractJsonQueryServlet {

    private final Logger log = LoggerFactory.getLogger(ModuleListingServlet.class);

    @Override
    protected String getQueryLanguage() {
        // While XPATH is deprecated in JCR 2.0, it's still supported (and recommended) in apache oak
        return Query.XPATH;
    }

    @Override
    protected String getQuery(SlingHttpServletRequest request) {
        String searchParam = paramValue(request, "search", "");
        String keyParam = paramValue(request, "key");
        String directionParam = paramValue(request, "direction");
        String[] productIds = request.getParameterValues("product");
        String[] productVersionIds = request.getParameterValues("productversion");
        String[] types = request.getParameterValues("type");
        String[] repoParam = request.getParameterValues("repo");
        String contentTypeParam = paramValue(request, "ctype");
        String[] statusParam = request.getParameterValues("status");

        if(keyParam==null || keyParam.contains("Uploaded")){
            keyParam = "pant:dateUploaded";
        }else if (keyParam.contains("Title")) {
            keyParam = "jcr:title";
        } else if (keyParam.contains("Published")){
            keyParam = "pant:datePublished";
        } else if (keyParam.contains("Module")){            
            keyParam = "pant:moduleType";
        } else if (keyParam.contains("Updated")){
            keyParam = JcrConstants.JCR_LASTMODIFIED;
        }

        // Transform contentTypeParam to map to the JCR type
        if (contentTypeParam != null) {
            if (contentTypeParam.toLowerCase().contains("module")) {
                contentTypeParam = "pant:module";
            } else if (contentTypeParam.toLowerCase().contains("assembly")) {
                contentTypeParam = "pant:assembly";
            }
        }

        if ("desc".equals(directionParam)) {
            directionParam = "descending";
        } else {
            directionParam = "ascending";
        }

        // Add all product revisions resolved from product ids
        try {
            productVersionIds = ArrayUtils.addAll(productVersionIds,
                    resolveProductVersions(request.getResourceResolver(), productIds));
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }

        StringBuilder queryBuilder = null;
        if (repoParam != null) {
            // encode if repo name contains space.
            // https://www.mail-archive.com/users@jackrabbit.apache.org/msg05782.html
            String repos = Arrays.stream(repoParam)
                    .map( repo -> {
                        return ISO9075.encode(repo);
                    })
                    .collect(Collectors.joining(" | "));
            String contentType = contentTypeParam != null ? contentTypeParam : "pant:document";

            queryBuilder = new StringBuilder()
                    .append("/jcr:root/content/repositories/(" + repos + ")//element(*, " + contentType + ")");
        } else {
            queryBuilder = new StringBuilder()
                    .append("/jcr:root/content/repositories//element(*, pant:document)");
        }

        List<StringBuilder> queryFilters = newArrayListWithCapacity(5);

        // only filter by text if provided
        if (searchParam.length() > 0) {
            StringBuilder textFilter = new StringBuilder()
                    .append("(")
                    .append("jcr:like(*/*/*/*/metadata/@jcr:title,'%" + searchParam + "%') ")
                    .append("or jcr:like(*/*/*/*/metadata/@jcr:description,'%" + searchParam + "%')")
                    .append("or jcr:like(*/*/*/*/cached_html/jcr:content/@jcr:data,'%" + searchParam + "%')")
                    .append(")");
            queryFilters.add(textFilter);
        }

        // product version filter
        if (productVersionIds != null && productVersionIds.length > 0) {
            StringBuilder productVersionCondition = new StringBuilder();
            List<String> conditions = Arrays.stream(productVersionIds)
                    .map(id -> {
                        return "*/*/*/*/metadata/@productVersion = '" + id + "'";
                    })
                    .collect(toList());
            productVersionCondition.append("(" + StringUtils.join(conditions, " or ") + ")");
            queryFilters.add(productVersionCondition);
        }

        // Content type filter
        if(types != null && types.length > 0) {
            StringBuilder contentTypeCondition = new StringBuilder();
            List<String> conditions = Arrays.stream(types)
                    .map( type -> {
                        if (type.equalsIgnoreCase("assembly")) {
                            return "@jcr:primaryType = 'pant:assembly'";
                        } else {
                            return "*/*/*/*/metadata/@pant:moduleType = '" + type + "'";
                        }
                    })
                    .collect(toList());
            contentTypeCondition.append("(" + StringUtils.join(conditions, " or ") + ")");
            queryFilters.add(contentTypeCondition);
        }

        // Status filter
        if (statusParam != null && statusParam.length < 2) {
            StringBuilder statusCondition = new StringBuilder();
            if (statusParam[0].equalsIgnoreCase("draft")) {
                statusCondition.append("*/*/draft/@pant:hash");
            } else {
                statusCondition.append("*/*/released/@pant:hash");
            }
            queryFilters.add(statusCondition);
        }

        // join all the available conditions
        if(queryFilters.size() > 0) {
            queryBuilder.append("[")
                    .append(StringUtils.join(queryFilters, " and "))
                    .append("]");
        }

        if(!isNullOrEmpty(keyParam) && !isNullOrEmpty(directionParam)) {
            //TODO: add condictional for order by
            queryBuilder.append(" order by */*/*/*/metadata/@")
                    .append(keyParam)
                    .append(" ")
                    .append(directionParam);
        }

        log.info("Executing module query: " + queryBuilder.toString());
        return queryBuilder.toString();
    }

    /**
     * Auxiliary method to resolve product version ids from a single product id.
     * @param resourceResolver The resource resolver to use to resolve the product version ids
     * @param productIds The collection of product ids to resolve
     * @return An array containing all product version ids which belong to any of the products which
     * ids has been passed in the parameters
     * @throws RepositoryException If there is a problem doing the resolution
     */
    private String[] resolveProductVersions(ResourceResolver resourceResolver, String[] productIds)
            throws RepositoryException {
        if(productIds == null || productIds.length == 0) {
            return new String[]{};
        }

        JcrQueryHelper queryHelper = new JcrQueryHelper(resourceResolver);

        // product conditions
        String productCondition = "";
        List<String> conditions = Arrays.stream(productIds)
                .map(id -> "product.[jcr:uuid] = '" + id + "'")
                .collect(toList());
        productCondition = "AND (" + StringUtils.join(conditions, " OR ") + ") ";

        StringBuilder query = new StringBuilder()
                .append("SELECT pv.* from [pant:productVersion] AS pv ")
                .append("INNER JOIN [pant:product] AS product ON ISDESCENDANTNODE(pv, product) ")
                .append("WHERE ISDESCENDANTNODE(product, '/content/products') ")
                .append(productCondition);

        // TODO Right now this queries for everything, complex queries with lots of products might not scale
        Stream<Resource> results = queryHelper.query(query.toString());
        return results.map(resource -> resource.getValueMap().get("jcr:uuid"))
                .collect(toList())
                .toArray(new String[]{});
    }

    @Override
    protected Map<String, Object> resourceToMap(Resource resource) {
        Module module = resource.adaptTo(Module.class);

        String variantName = module.getWorkspace().getCanonicalVariantName();

        Optional<ModuleMetadata> draftMetadata = module.getDraftMetadata(DEFAULT_MODULE_LOCALE, variantName);
        Optional<ModuleMetadata> releasedMetadata = module.getReleasedMetadata(DEFAULT_MODULE_LOCALE, variantName);
        Optional<HashableFileResource> sourceFile =
                Child.from(module)
                        .toChild(m -> m.locale(DEFAULT_MODULE_LOCALE))
                        .toChild(ModuleLocale::source)
                        .toChild(sourceContent -> sourceContent.draft().isPresent() ? sourceContent.draft() : sourceContent.released())
                        .asOptional();

        // get Validations
        Optional<Validations> draftValidations = module.getValidations(DEFAULT_MODULE_LOCALE, variantName, "draft");
        Optional<Validations> releasedValidations = module.getValidations(DEFAULT_MODULE_LOCALE, variantName, "released");

        // TODO Need some DTOs to convert to maps
        Map<String, Object> m = super.resourceToMap(resource);
        String resourcePath = resource.getPath();
        m.put("name", resource.getName());        
        // TODO need to provide both released and draft to the api caller        
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        
        //logic for file name is present in ModuleVersionUpload.java
        if ("pantheon/assembly".equals(resource.getResourceType())) {
            m.put("moduleType", "ASSEMBLY"); // This is temporary while assembly and modules are munged together.
        } else {
            if (draftMetadata.isPresent() && draftMetadata.get().moduleType().get() != null) {
                m.put("moduleType", draftMetadata.get().moduleType().get());
            } else if (releasedMetadata.isPresent() && releasedMetadata.get().moduleType().get() != null) {
                m.put("moduleType", releasedMetadata.get().moduleType().get());
            } else {
                m.put("moduleType", "-");
            }
        }

        if(sourceFile.isPresent() && sourceFile.get().created().get() != null){
            m.put("pant:dateUploaded", sdf.format(sourceFile.get().created().get().getTime()));
        }else if(releasedMetadata.isPresent() && releasedMetadata.get().dateUploaded().get()!=null){
            m.put("pant:dateUploaded",sdf.format(releasedMetadata.get().dateUploaded().get().getTime()));
        }else{
            m.put("pant:dateUploaded","-");
        }

        if(releasedMetadata.isPresent() && releasedMetadata.get().datePublished().get()!=null){            
            m.put("pant:publishedDate",sdf.format(releasedMetadata.get().datePublished().get().getTime()));            
        }else{
            m.put("pant:publishedDate","-");
        }

        if(draftMetadata.isPresent() && draftMetadata.get().title().get()!=null){
            m.put("jcr:title",draftMetadata.get().title().get());
        }else if(releasedMetadata.isPresent() && releasedMetadata.get().title().get()!=null){
            m.put("jcr:title",releasedMetadata.get().title().get());
        }else{
            m.put("jcr:title","-");
        }

        if(draftMetadata.isPresent() && draftMetadata.get().description().get()!=null){
            m.put("jcr:description",draftMetadata.get().description().get());
        }else if(releasedMetadata.isPresent() && releasedMetadata.get().description().get()!=null){
            m.put("jcr:description",releasedMetadata.get().description().get());
        }else{
            m.put("jcr:description","-");
        }

        if(draftMetadata.isPresent() && draftMetadata.get().productVersion().get()!=null){
            m.put("productVersion",draftMetadata.get().productVersion().get());
        }else if(releasedMetadata.isPresent() && releasedMetadata.get().productVersion().get()!=null){
            m.put("productVersion",releasedMetadata.get().productVersion().get());
        }else{
            m.put("productVersion","-");
        }
        // Assume the path is something like: /content/<something>/my/resource/path
        m.put("pant:transientPath", resourcePath.substring("/content/".length()));
        // Example path: /content/repositories/ben_2019-04-11_16-15-15/shared/attributes.module.adoc
        String[] fragments = resourcePath.split("/");
        // Example fragments: ["", "content", "repositories", "ben_2019-04-11_16-15-15", "shared", "attributes.module.adoc"]
        m.put("pant:transientSource", fragments[2]);
        if (!"modules".equals(fragments[2])) {
            m.put("pant:transientSourceName", fragments[3]);
        }

        if (!variantName.isEmpty()) {
            m.put("variant", variantName);
        }

        // Render xrefValidations as array.
        List<Map> xrefValidationsList = new ArrayList<>();
        if(draftValidations.isPresent() && draftValidations.get().listChildren().next().getValueMap().get("pant:message")!=null){
            m.put("validations",draftValidations.get().listChildren().next().getValueMap().get("pant:message"));
        }else if(releasedValidations.isPresent() && releasedValidations.get().listChildren().next().getValueMap().get("pant:message")!=null){
            m.put("validations",releasedValidations.get().listChildren().next().getValueMap().get("pant:message"));
        }else{
            m.put("validations","-");
        }
        log.trace(m.toString());
        return m;
    }
}
