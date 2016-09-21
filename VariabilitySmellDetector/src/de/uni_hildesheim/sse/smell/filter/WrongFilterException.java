package de.uni_hildesheim.sse.smell.filter;

import de.uni_hildesheim.sse.smell.data.IDataElement;

/**
 * An exception thrown by filters if the data is not suitable for them.
 * @author Adam Krafczyk
 */
public class WrongFilterException extends FilterException {
    
    private static final long serialVersionUID = 1L;
    
    public WrongFilterException(Class<? extends IFilter> filter, Class<? extends IDataElement> expectedInput,
        Object actualInput) {
        
        super("Filter " + filter.getName() + " expects input of type " + expectedInput + ", but was "
            + actualInput.getClass());
    }

}
