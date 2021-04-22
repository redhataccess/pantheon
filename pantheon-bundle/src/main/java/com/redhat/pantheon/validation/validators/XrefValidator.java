package com.redhat.pantheon.validation.validators;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.validation.helper.XrefValidationHelper;
import com.redhat.pantheon.validation.model.ErrorDetails;
import com.redhat.pantheon.validation.model.Violations;
import org.apache.sling.api.resource.Resource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.List;

/**
 * This is a sample validator that
 * <p>
 * <ol>Provides logic to check passed object is null or not </ol>
 * <ol>Accepts the data via setter when injected</ol>
 * <ol>Accepts the data via constructor instantiated normally</ol>
 * <ol>Returns its unique name via getName()</ol>
 * <ol>Returns the constraint violations via {@see Violations} instance</ol>
 * </p>
 */
@Component(service = XrefValidator.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Provides validation services",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        }
)
public class XrefValidator implements Validator {

    private String content;

    private DocumentVariant documentVariant;

    private static final Logger log = LoggerFactory.getLogger(XrefValidator.class.getName());

    public XrefValidator() {
    }

    public XrefValidator(DocumentVariant documentVariant, String content) {
        this.content = content;
        this.documentVariant = documentVariant;
    }

    @Override
    public Violations validate() {
        return checkIfXrefValid(new Violations());
    }

    private Violations checkIfXrefValid(Violations violations) {
        return violations.add(PantheonConstants.TYPE_XREF,
                checkXref());
    }

    /**
     * Process xrefs
     *
     * @return
     */
    private ErrorDetails checkXref() {
        ErrorDetails errorDetails = new ErrorDetails();
        try {
            XrefValidationHelper xrefValidationHelper = new XrefValidationHelper();
            List<String>  xrefTargets = xrefValidationHelper.getObjectsToValidate();
            if(null == xrefTargets || xrefTargets.size()==0){
                return errorDetails;
            }
            for (String xref : xrefTargets) {
                if(!xref.endsWith(".adoc")) {
                    if(validateIfAnchor(content,xref)<1){ // If it is anchor, it has not yet validated,
                        errorDetails.add(xref);
                    }
                }else {
                    errorDetails.add(xref); // if it is a adoc file, it's already processed and validated
                }
            }
        }
        catch (Exception ex){
            log.error("error at validation occured",ex);
        }
        return errorDetails;
    }

    /**
     * if path is an anchor, validate
     *
     * @param content
     * @param xref
     * @return
     * @throws RepositoryException
     */
    private int validateIfAnchor(String content, String xref) throws RepositoryException {
            //if path is an anchor
        Document doc = Jsoup.parse(content);
        return doc.getElementsByAttributeValue("id",xref).size();
    }

    /**
     * Gets  unique name of the validator
     *
     * @return the name of the validator
     */
    @Override
    public String getName() {
        return XrefValidator.class.getClass().getName();
    }

}
