package com.redhat.pantheon.validation.validators;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.jcr.JcrQueryHelper;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.validation.helper.XrefValidationHelper;
import com.redhat.pantheon.validation.model.ErrorDetails;
import com.redhat.pantheon.validation.model.Violations;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
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
        if (isValidXref()) {
            return violations;
        }
        return violations.add(PantheonConstants.VALID_XREF,
                new ErrorDetails().add("invalid Cross reference(s) exists in the document"));
    }

    private boolean isValidXref() {
        try {
                Document doc = Jsoup.parse(content);
                List<String>  xrefTargets = XrefValidationHelper.getObjectsToValidate(this.documentVariant.uuid().get());
                Elements resultLinks = doc.select("a");
                if(null == xrefTargets || xrefTargets.size()==0){
                    return true;
                }
                int count = 0;
                for (String xref : xrefTargets) {
                    count = getXrefCounts(resultLinks, count, xref);
                }
                return count == XrefValidationHelper.getObjectsToValidate(this.documentVariant.uuid().get()).size() ? true : false;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    private int getXrefCounts(Elements resultLinks, int count, String xref) throws RepositoryException {
        if(xref.endsWith(".adoc")){
            JcrQueryHelper jcrQueryHelper = new JcrQueryHelper(this.documentVariant.getResourceResolver());
            Resource resource = documentVariant.getParentLocale().getParent().getParent();
            String[] resourceFragment = xref.split("/");

            for(String rf:resourceFragment){
                switch (rf){
                    case "..":resource = resource.getParent(); break;   // TODO: fails in case dependent document not yet uploaded
                    default: resource = resource.getChild(rf); break;
                }
            }

            count += resource!=null ? 1 :0;
        } else {   //if path is an anchor
            count += (int) resultLinks.eachAttr("href").stream().filter(s->s.endsWith(xref)).count() > 0?1:0;
        }
        return count;
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
