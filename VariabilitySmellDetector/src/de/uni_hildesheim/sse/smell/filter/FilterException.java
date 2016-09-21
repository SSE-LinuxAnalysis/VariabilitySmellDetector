package de.uni_hildesheim.sse.smell.filter;

/**
 * An exception thrown by filters if an erros occurs.
 * @author El-Sharkawy
 */
public class FilterException extends Exception {
    
    private static final long serialVersionUID = 5761475464254599457L;

    public FilterException(Exception occuredException) {
        super(occuredException);
    }
    
    protected FilterException(String msg) {
        super(msg);
    }

}
