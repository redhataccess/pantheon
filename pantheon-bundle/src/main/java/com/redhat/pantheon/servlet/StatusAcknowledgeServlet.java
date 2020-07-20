package com.redhat.pantheon.servlet;

import com.redhat.pantheon.model.Acknowledgment;
import com.redhat.pantheon.model.document.AckStatus;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.validation.validators.NotNullValidator;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.sling.query.SlingQuery.$;

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
            getLogger().error("The request did not provide all the fields " + acknowledgment.toString());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "All the fields are required");
            return;
        }
        try {
            Resource resource = getResourceByUuid(acknowledgment.getId(), request);
            ModuleVariant moduleVariant = resource.adaptTo(ModuleVariant.class);

            // TODO The empty string below needs to contain the variant name. Either the ID needs to now point
            //  to a specific Module version (within a variant), or the variant needs to be passed in the request
            processAcknowledgementRequest(acknowledgment, moduleVariant,
                    request.getUserPrincipal().getName());

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
     * @param moduleVariant moduleVariant corresponding to the UUID in the request data
     * @param lastModifiedBy
     * @throws PersistenceException signals that request data could not be saved
     */
    private void processAcknowledgementRequest(Acknowledgment acknowledgement, ModuleVariant moduleVariant,
            String lastModifiedBy) throws PersistenceException {

        createStatusNode(moduleVariant, acknowledgement, lastModifiedBy);
    }

    private boolean hasLocale(List<Resource> moduleLocale, String locale) {
        return moduleLocale.stream().anyMatch(ml -> ml.getName().equalsIgnoreCase(locale));
    }

    private void createStatusNode(ModuleVariant moduleVariant,
                                  Acknowledgment acknowledgement, String lastModifiedBy) throws PersistenceException {
        AckStatus status = createStatusNode(moduleVariant);
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
     * Creates or retrieves status node based on whether a published version exists. If a published version exists,
     * either create a new status node if it does not exist
     *
     * @param moduleVariant
     * @return
     */
    private AckStatus createStatusNode(ModuleVariant moduleVariant) {
        if (moduleVariant.released().isPresent()) {
            return moduleVariant.released().get().ackStatus().getOrCreate();
        }
        return moduleVariant.draft().get().ackStatus().getOrCreate();
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
