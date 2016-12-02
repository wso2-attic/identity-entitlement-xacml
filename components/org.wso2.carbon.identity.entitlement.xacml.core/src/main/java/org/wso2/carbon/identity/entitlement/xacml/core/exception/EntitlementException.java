package org.wso2.carbon.identity.entitlement.xacml.core.exception;


/**
 *  This is a custom exception class for entitlement module.
 *  All the ejections will be thrown in this module as  EntitlementException
 */
public class EntitlementException extends Exception {

    private static final long serialVersionUID = 4671622091461340493L;
    private String message;

    public EntitlementException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public EntitlementException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
