package com.redhat.pantheon.validation.events;

import com.redhat.pantheon.validation.model.CombinedViolations;

/**
 * The listener interface for event raised when a set of validators complete
 *  their execution.
 */
public interface ValidationsCompleteListener {
      /**
       * When all the validators are executed, this event is raised. This '
       *  is not a Sling event
       *
       * @param combinedViolations the result of all the executed validations
       */
      void onValidationsComplete(CombinedViolations combinedViolations);
}
