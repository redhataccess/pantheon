package com.redhat.pantheon.validation.helper;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.document.DocumentVersion;
import com.redhat.pantheon.validation.model.*;
import com.redhat.pantheon.validation.validators.XrefValidator;
import org.apache.sling.api.resource.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/**
 * Validation helper for mapping violations and validations and creating validation node
 */
public class ValidationHelper {
    Logger logger = LoggerFactory.getLogger(ValidationHelper.class);
    public void createXrefValidationNode(DocumentVersion documentVersion, String content) throws PersistenceException {
        Violations violations = new XrefValidator(documentVersion.getParent(), content).validate();
        // Get xrefTargetMap
        HashMap<String, ArrayList<String>> xrefTargetsMap = XrefValidationHelper.getObjectsToValidate();

        if (xrefTargetsMap != null && xrefTargetsMap.containsKey(documentVersion.getParent().uuid().get())) {
            Validations validations = documentVersion.validations().getOrCreate();
            if(null != validations.validationType(PantheonConstants.TYPE_XREF).get()){
                try {
                    validations.validationType(PantheonConstants.TYPE_XREF).get().delete();
                } catch (Exception e) {
                    logger.error("error while validation node creation",e);
                }
            }

            if(violations.hasViolations()) {
                Validation validation;
                ErrorDetails errorDetails = violations.get(PantheonConstants.TYPE_XREF);
                if(null == errorDetails || errorDetails.length() ==0){
                    return;
                }
                ValidationType validationType = validations.validationType(PantheonConstants.TYPE_XREF).getOrCreate();
                for(int ind=0; ind< errorDetails.length();ind++) {
                    validation = validationType.page(ind+1).getOrCreate();
                    validation.setValidation(violations, ind);
                }
            }
        }

    }
}