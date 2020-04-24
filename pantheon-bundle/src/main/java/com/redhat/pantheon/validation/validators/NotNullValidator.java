package com.redhat.pantheon.validation.validators;

import com.redhat.pantheon.validation.model.ErrorDetails;
import com.redhat.pantheon.validation.model.Violations;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import java.util.List;
import java.util.Objects;

/**
 *  This is a sample validator that
 *  <p>
 *      <ol>Provides logic to check passed object is null or not </ol>
 *      <ol>Accepts the data via setter when injected</ol>
 *      <ol>Accepts the data via constructor instantiated normally</ol>
 *      <ol>Returns its unique name via getName()</ol>
 *      <ol>Returns the constraint violations via {@see Violations} instance</ol>
 *  </p>
 */
@Component( service = NotNullValidator.class,
        property = {
            Constants.SERVICE_DESCRIPTION + "=Provides validation services",
            Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        }
)
public class NotNullValidator implements Validator {

    private List<Object> objectsToValidate;
   public NotNullValidator(){}
    public NotNullValidator(List<Object> objectsToValidate) {
        this.objectsToValidate = objectsToValidate;
    }
    @Override
    public Violations validate() {
        return checkIfNull(new Violations());
    }

    private Violations checkIfNull(Violations violations) {
        if (!isNull()) {
            return violations;
        }
        return violations.add("Not null validation failed",
                new ErrorDetails().add("One of the objects in the list has null value"));
    }

    private boolean isNull() {
        return getObjectsToValidate().stream().anyMatch(Objects::isNull);
    }

    /**
     * Gets  unique name of the validator
     *
     * @return the name of the validator
     */
    @Override
    public String getName() {
        return "NotNullValidator";
    }

    public List<Object> getObjectsToValidate() {
        return objectsToValidate;
    }

    public void setObjectsToValidate(List<Object> objectsToValidate) {
        this.objectsToValidate = objectsToValidate;
    }
}
