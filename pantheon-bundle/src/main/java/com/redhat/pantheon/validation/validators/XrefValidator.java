package com.redhat.pantheon.validation.validators;

import com.redhat.pantheon.model.Xref;
import com.redhat.pantheon.validation.model.ErrorDetails;
import com.redhat.pantheon.validation.model.Violations;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;

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

    private static List<String> xRefs = new ArrayList<>();

    private String content;

    public XrefValidator() {
    }

    public XrefValidator(String content) {
        this.content = content;
    }

    @Override
    public Violations validate() {
        return checkIfXrefValid(new Violations());
    }

    private Violations checkIfXrefValid(Violations violations) {
        if (!isValidXref()) {
            return violations;
        }
        return violations.add("Not a valid Xref",
                new ErrorDetails().add("invalid Cross reference(s) exists in the document"));
    }

    private boolean isValidXref() {
        try {
                Document doc = Jsoup.parse(content);
                Elements resultLinks = doc.select("h2");
                int count = 0;
                for (String xref : xRefs) {
                    count += (int) resultLinks.eachAttr("id").stream().filter(s -> s.equalsIgnoreCase(xref)).count();
                }
                return count == xRefs.size() ? true : false;
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
        return "XrefValidator";
    }

    public List<String> getObjectsToValidate() {
        return xRefs;
    }

    public static void setObjectsToValidate(List<String> objectsToValidate) {
        if(objectsToValidate.size()>0)
            xRefs.addAll(objectsToValidate);
    }
}
