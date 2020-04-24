package com.redhat.pantheon.validation.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for error details. Error details are sub category for
 *  violations i.e. validation failures.
 *  <p>
 *      A single validator might have multiple
 *       internal validations. For example, a links validator might not only check
 *      whether the link is valid i.e. it is as per HTTP URL pattern but also
 *      is it accessible or not.
 *  </p>
 *  <p>
 *      In such a case, the validator can create two instances of this class
 *      and add the link pattern check failure to the first and accessibilty
 *      check failure to the second
 *  </p>
 * @author A.P. Rajshekhar
 */
public class ErrorDetails {
    private List<String> details = new ArrayList<>();

    /**
     * Add error details.
     *
     * @param detail the detail
     * @return the error details
     */
    public ErrorDetails add(String detail){
        details.add(detail);
        return this;
    }

    /**
     * Get details string.
     *
     * @return the string
     */
    public String getDetails(){
        return String.join("\n", details);
    }
}
