package de.uni_hildesheim.sse.smell.util;

/**
 * This exception is thrown by a {@link VariableToNumberConverter} if a variable
 * is not found in a DIMACS model.
 * 
 * @author Adam Krafczyk
 */
public class VarNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;
    
    private String name;
    
    public VarNotFoundException(String name) {
        super("Cannot find variable \"" + name + "\"");
        this.name = name;
    }
    
    /**
     * @return The name of the variable that is not found.
     */
    public String getName() {
        return name;
    }


}
