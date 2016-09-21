package de.uni_hildesheim.sse.smell.data;

/**
 * The type of preprocessor block.
 * 
 * @author Adam Krafczyk
 */
public enum ConditionType {

    IF("if"),
    
    ELSEIF("elseif"),
    
    ELSE("else");
    
    private String name;
    
    private ConditionType(String name) {
        this.name = name;
    }
    
    public static ConditionType getConditionType(String name) {
        return valueOf(name.toUpperCase());
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
