package de.uni_hildesheim.sse.smell.util;

/**
 * This exception is thrown, if parsing a constraint fails.
 * 
 * @author Adam Krafczyk
 */
public class ConstraintException extends Exception {

    private static final long serialVersionUID = -2153432140783061540L;

    public ConstraintException(String message) {
        super(message);
    }
    
    public ConstraintException(Throwable cause) {
        super(cause);
    }
    
}
