package com.redhat.pantheon.validation.helper;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.document.DocumentVersion;
import com.redhat.pantheon.validation.model.*;
import com.redhat.pantheon.validation.validators.XrefValidator;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * Validation helper for mapping violations and validations and creating validation node
 */
public class ValidationHelper {
    public void createXrefValidationNode(DocumentVersion documentVersion, String content){
        Violations violations = new XrefValidator(documentVersion.getParent(), content).validate();
        if(violations.hasViolations()) {
            ValidationType validationType = documentVersion.validations().getOrCreate().validationType().getOrCreate();
            Validation validation;
            ErrorDetails errorDetails = violations.get(PantheonConstants.TYPE_XREF);
            if(errorDetails == null){
                return;
            }
            for(int ind=0; ind< errorDetails.length();ind++) {
                validation = validationType.page(ind+1).getOrCreate();
                validation.setValidation(violations, ind);
            }
        }
    }
}