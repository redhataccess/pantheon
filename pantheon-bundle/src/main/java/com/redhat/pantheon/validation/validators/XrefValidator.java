package com.redhat.pantheon.validation.validators;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.validation.helper.XrefValidationHelper;
import com.redhat.pantheon.validation.model.ErrorDetails;
import com.redhat.pantheon.validation.model.Violations;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

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

    private String uid;

    public XrefValidator() {
    }

    public XrefValidator(String uid, String content) {
        this.content = content;
        this.uid = uid;
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
                Elements resultLinks = doc.select("h2");
                List<String>  filepaths = XrefValidationHelper.getObjectsToValidate(this.uid);
                if(null == filepaths || filepaths.size()==0){
                    return true;
                }
                int count = 0;
                for (String xref : filepaths) {
                    count += (int) resultLinks.eachAttr("id").stream().filter(s -> s.equalsIgnoreCase(xref)).count();
                }
                System.out.println(count + " xref : "+ XrefValidationHelper.getObjectsToValidate(this.uid));
                return count == XrefValidationHelper.getObjectsToValidate(this.uid).size() ? true : false;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
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
