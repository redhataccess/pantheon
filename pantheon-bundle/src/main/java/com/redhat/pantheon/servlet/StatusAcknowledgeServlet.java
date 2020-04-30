package com.redhat.pantheon.servlet;

import com.redhat.pantheon.extension.Events;
import com.redhat.pantheon.model.Acknowledgment;
import com.redhat.pantheon.model.module.AckStatus;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.validation.validators.NotNullValidator;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import static org.apache.sling.query.SlingQuery.$;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple servlet that saves the status acknowledgement sent by an endsystem to a status node
 *
 * @author A.P.Rajshekhar
 */
@Component(
        service = Servlet.class,
        property = {
            Constants.SERVICE_DESCRIPTION + "=Servlet which accepts acknowledgement and status for publish and unpublish actions for a  Module",
            "sling.servlet.methods=" + HttpConstants.METHOD_POST,
            Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        }
)
@SlingServletPaths(value = "/api/status")
public class StatusAcknowledgeServlet extends AbstractJsonPostOrPutServlet<Acknowledgment> {

    private final Logger logger = LoggerFactory.getLogger(StatusAcknowledgeServlet.class);
    //the injection can be done via CTOR, setter or field. For sample validator, I have used field injection
    @Reference
    private NotNullValidator notNullValidator;


    public StatusAcknowledgeServlet() {
        super(Acknowledgment.class);
    }

    @Override
    protected void processPost(SlingHttpServletRequest request, SlingHttpServletResponse response, Acknowledgment acknowledgment)
            throws ServletException, IOException {
        if (isObjectNullOrEmpty(acknowledgment)) {
            getLogger().error("The request did not provide all the fiields " + acknowledgment.toString());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "All the fields are required");
            return;
        }
        try {
            Resource resource = getResourceByUuid(acknowledgment.getId(), request);
            Module module = resource.adaptTo(Module.class);
            List<Resource> moduleLocale = $(module).find("pant:moduleLocale").asList();

            if (!hasLocale(moduleLocale, "en_US")) {
                getLogger().error("The module with id=" + acknowledgment.getId() + " does not have en_US locale");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Locale other than en_US is not supported");
                return;
            }
            processAcknowledgementRequest(acknowledgment, module, moduleLocale, request.getUserPrincipal().getName());

        } catch (RepositoryException | PersistenceException e) {
            getLogger().error("The request could not be processed because of error=" + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Checks whether all the fields are present
     *
     * @param acknowledgement the acknowledement object containing request data
     * @return true if not all fields have data else return false
     */
    private boolean isObjectNullOrEmpty(Acknowledgment acknowledgement) {
        return null == acknowledgement || validate(acknowledgement);

    }

    private boolean validate(Acknowledgment acknowledgement) {
        //how the data is passed to validator is left to the implementing class. In the case of sample NotNullValidator
        //data is passed via setter
        getNotNullValidator().setObjectsToValidate(Stream.of(acknowledgement.getId(), acknowledgement.getMessage(),
                acknowledgement.getSender(), acknowledgement.getStatus())
                .collect(Collectors.toList()));
        return getNotNullValidator().validate().hasViolations();
    }

    /**
     * Process the data in acknowldegment and create node if the module for which the acknowledgement has data contains
     * supported locale
     *
     * @param acknowledgement request data
     * @param module module corresponding to the UUID in the request data
     * @param moduleLocale list of locales in the module
     * @throws PersistenceException signals that request data could not be saved
     */
    private void processAcknowledgementRequest(Acknowledgment acknowledgement, Module module,
            List<Resource> moduleLocale, String lastModifiedBy) throws PersistenceException {

        for (Resource locale : moduleLocale) {
            //defensive programming: double check that only for en_US locale the status node is created
            if (locale.getName().equalsIgnoreCase("en_US")) {
                createStatusNode(locale, module, acknowledgement, lastModifiedBy);
                break;
            }
        }
    }

    private boolean hasLocale(List<Resource> moduleLocale, String locale) {
        return moduleLocale.stream().anyMatch(ml -> ml.getName().equalsIgnoreCase(locale));
    }

    private void createStatusNode(Resource moduleLocale, Module module, Acknowledgment acknowledgement, String lastModifiedBy) throws PersistenceException {
        Locale locale = LocaleUtils.toLocale(moduleLocale.getName());
        AckStatus status = createStatusNode(module, locale);
        status.status().set(acknowledgement.getStatus());
        status.message().set(acknowledgement.getMessage());
        status.sender().set(acknowledgement.getSender());
        Calendar now = Calendar.getInstance();
        status.dateModified().set(now);
        // update lastModifiedBy
        status.lastModifiedBy().set(lastModifiedBy);
        status.getResourceResolver().commit();
    }

    /**
     * Creates or retrives status node based on whether a published version exists. If a published version exists,
     * either create a new status node if it does not exist
     *
     * @param module
     * @param locale
     * @return
     */
    private AckStatus createStatusNode(Module module, Locale locale) {
        if (module.getReleasedVersion(locale).isPresent()) {
            return module.getReleasedVersion(locale).get().ackStatus().getOrCreate();
        }
        return module.getDraftVersion(locale).get().ackStatus().getOrCreate();
    }

    /**
     * Retrieves the module corresponding to the uuid
     *
     * @param uuid id of the module
     * @param request sling request
     * @return resource correponding to the uuid
     * @throws RepositoryException
     */
    private Resource getResourceByUuid(String uuid, SlingHttpServletRequest request) throws RepositoryException {
        Node foundNode = request.getResourceResolver()
                .adaptTo(Session.class)
                .getNodeByIdentifier(uuid);

        return request.getResourceResolver()
                .getResource(foundNode.getPath());
    }

    /**
     * Gets logger.
     *
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }

    public NotNullValidator getNotNullValidator() {
        return notNullValidator;
    }

    public void setNotNullValidator(NotNullValidator notNullValidator) {
        this.notNullValidator = notNullValidator;
    }


}
