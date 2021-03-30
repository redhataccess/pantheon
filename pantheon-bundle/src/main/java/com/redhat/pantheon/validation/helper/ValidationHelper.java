package com.redhat.pantheon.validation.helper;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.document.DocumentVersion;
import com.redhat.pantheon.validation.model.Validation;
import com.redhat.pantheon.validation.model.Validations;
import com.redhat.pantheon.validation.model.Violations;
import com.redhat.pantheon.validation.validators.XrefValidator;

/**
 * Validation helper for mapping violations and validations and creating validation node
 */
public class ValidationHelper {
    public void createXrefValidationNode(DocumentVersion documentVersion, String content){
        Violations violations = new XrefValidator(documentVersion.getParent().uuid().get(), content).validate();
        if(violations.hasViolations()) {
            Validations validationResult = documentVersion.validations().getOrCreate();
            Validation validation = validationResult.page(PantheonConstants.TYPE_XREF).getOrCreate();
            validation.setValidation(violations);
        }
    }
}