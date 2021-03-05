package com.redhat.pantheon.servlet.module;

import com.ibm.icu.util.ULocale;
import com.redhat.pantheon.jcr.JcrQueryHelper;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.model.module.ModuleVersion;
import com.redhat.pantheon.servlet.MapConverters;
import com.redhat.pantheon.servlet.util.SlingPathSuffix;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.redhat.pantheon.servlet.ServletUtils.writeAsJson;

/**
 * An API endpoint for finding module information using a vanity url format which takes into account the content's
 * product metadata, and locale. The format is as follows:<br><br>
 *
 * '/api/module/vanity.json/{locale}/{product}/{version}/{vanityUrlFragment}'<br><br>
 *
 * It will also resolve content with the following format: <br><br>
 *
 * '/api/module/vanity.json/{locale}/{product}/{version}/{moduleUUID}'
 *
 * <br>
 * @see VariantJsonServlet for a servlet returning the same information
 * @author Carlos Munoz
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet to facilitate GET operation which accepts several path parameters to fetch module variant data",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/api/module/vanity")
// /api/module/vanity.json/{locale}/{productLabel}/{versionLabel}/{vanityUrl}
public class VanityVariantJsonServlet extends SlingSafeMethodsServlet {

    private final SlingPathSuffix suffix = new SlingPathSuffix("/{locale}/{productLabel}/{versionLabel}/{vanityUrl}");

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        Map<String, String> params = suffix.getParameters(request);
        String locale = params.get("locale");
        String productLabel = params.get("productLabel");
        String versionLabel = params.get("versionLabel");
        String vanityUrl = params.get("vanityUrl");

        JcrQueryHelper queryHelper = new JcrQueryHelper(request.getResourceResolver());
        try {

            // Find the metadata with the right vanity url,
            Optional<ModuleVersion> moduleVersion = queryHelper.query(
                    "select * from [pant:moduleVersion] as v where v.[metadata/urlFragment] = '/" + vanityUrl + "'")
                    .map(resource -> SlingModels.getModel(resource, ModuleVersion.class))
                    // the right locale,
                    .filter(modVer -> {
                        String normalizedLocaleCode = ULocale.canonicalize(locale);
                        String normalizedModuleLocaleCode = ULocale.canonicalize(modVer.getParent().getParentLocale().getName());
                        return normalizedLocaleCode.equals(normalizedModuleLocaleCode);
                    })
                    // the right version,
                    .filter(modVer -> {
                        try {
                            return modVer.metadata().get().productVersion().getReference().name().get().equals(versionLabel);
                        } catch (RepositoryException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    // and the right product
                    .filter(modVer -> {
                        try {
                            return modVer.metadata().get().productVersion().getReference().getProduct().urlFragment().get().equals(productLabel);
                        } catch (RepositoryException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    // There should be 1 at most, but get the first if there are more
                    .findFirst();

            if(!moduleVersion.isPresent()) {
                // try to find by variant uuid, instead of vanity url
                moduleVersion = findModuleByUuid(queryHelper, productLabel, versionLabel, locale, vanityUrl);
            }

            if(!moduleVersion.isPresent()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Module version with vanity url '" + vanityUrl + "' not found");
                return;
            }
            else {
                // TODO This is traversing up to the variant to keep the compatibility with VariantJsonServlet
                //  it should be revisited if/after this api is deprecated
                writeAsJson(response, MapConverters.moduleVariantToMap(request, moduleVersion.get().getParent()));
            }

        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO This could just piggy back on the other variant json servlet (but then there is no validation)
    private Optional<ModuleVersion> findModuleByUuid(final JcrQueryHelper queryHelper,
                                                     final String productUrlFragment,
                                                     final String productVersionUrlFragment,
                                                     final String locale,
                                                     final String moduleVariantUuid) {
        try {
            return queryHelper.query("select * from [pant:moduleVariant] as moduleVariant WHERE moduleVariant.[jcr:uuid] = '" +
                    moduleVariantUuid + "'")
                    .map(resource -> SlingModels.getModel(resource, ModuleVariant.class))
                    // check the other parameters match
                    .filter(moduleVariant -> {
                        try {
                            return ULocale.canonicalize(moduleVariant.getParentLocale().getName()).equals(ULocale.canonicalize(locale))
                                    && moduleVariant.released().get().metadata().get().productVersion().getReference().urlFragment().get()
                                    .equals(productVersionUrlFragment)
                                    && moduleVariant.released().get().metadata().get().productVersion().getReference().getProduct()
                                    .urlFragment().get().equals(productUrlFragment);
                        } catch (RepositoryException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    // use the released version to keep compatibility with VariantJsonServlet
                    .map(moduleVariant -> moduleVariant.released().get())
                    .findFirst();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}
