package com.redhat.pantheon.validation.events;

import com.redhat.pantheon.extension.Event;
import com.redhat.pantheon.extension.EventProcessingExtension;
import com.redhat.pantheon.validation.model.CombinedViolations;
import com.redhat.pantheon.validation.model.ValidationClientDetails;
import com.redhat.pantheon.validation.validators.Validator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * The event processor for  {@see ValidationTrigger}.
 * <p>
 *     The payload of the event would have one or more validators' names. The processor
 *     would execute each of the validators by getting the validators from Selected validator service
 *     and add the validator result against each
 *     validator. Once all the validators are executed, the processor notifies all the
 *     listeners waiting for validation to be completed and pass the accumulated
 *     result.
 * </p>
 */
@Component(
        service = EventProcessingExtension.class
)
public class ValidionTriggerProcessor implements EventProcessingExtension {

    private ValidationsCompleteNotifierService validationsCompleteNotifierService;
    @Reference
    private SelectedValidationsService selectedValidationsService;
    /**
     * Instantiates a new Validion event processor by injecting the notifier service
     *
     * @param validationsCompleteNotifierService the notifier service to be invoked
     *                                          when all the validators in the event payload are executed
     */
    @Activate
    public ValidionTriggerProcessor(
            @Reference ValidationsCompleteNotifierService validationsCompleteNotifierService){
        this.validationsCompleteNotifierService = validationsCompleteNotifierService;
    }

    @Override
    public boolean canProcessEvent(Event event) {
        return true;
    }

    @Override
    public void processEvent(Event event) throws Exception {
        ValidationTrigger validationTriggerEvent = (ValidationTrigger) event;
        processValidation(validationTriggerEvent.getNames(), validationTriggerEvent.getComponent(), new CombinedViolations());
    }

    /**
     *  Executes all the validators provided in the SelectedValdations and combine all the results. The details of component
     *  that has triggered this event is set as a part of the combined result.
     *  Once all the  validators are executed, the notifier service is invoked to notify all the listeners that
     *   all the validators have been executed
     * @param validators the validators to be executed
     * @param validationClientDetails
     * @param combinedViolations combined result
     */
    private void processValidation(String validators, String validationClientDetails, CombinedViolations combinedViolations) {
        selectedValidationsService.getValidators()
                .stream().
                filter(validator -> checkValidatorNames(validator, validators))
                .forEach(validator -> combinedViolations.add(validator.getName(), validator.validate()));
        combinedViolations.setValidationClientDetails(new ValidationClientDetails(validationClientDetails));
        this.validationsCompleteNotifierService.notifyValidationsCompleteListeners(combinedViolations);
    }

    private boolean checkValidatorNames(Validator validator, String validators) {
        for (String name: validators.split(",")) {
            return name.equalsIgnoreCase(validator.getName());
        }
        return false;
    }
}
