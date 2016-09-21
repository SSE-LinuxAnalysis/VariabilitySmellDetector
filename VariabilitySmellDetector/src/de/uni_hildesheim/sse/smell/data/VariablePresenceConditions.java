package de.uni_hildesheim.sse.smell.data;

import java.util.LinkedList;
import java.util.List;

public class VariablePresenceConditions implements IDataElement {
    
    private String variable;
    
    private List<String> presenceConditions;
    
    public VariablePresenceConditions(String variable) {
        this.variable = variable;
        this.presenceConditions = new LinkedList<>();
    }
    
    public void addPresenceCondition(String pc) {
        presenceConditions.add(pc);
    }
    
    public List<String> getPresenceConditions() {
        return presenceConditions;
    }

    @Override
    public String toCsvLine(String delim) {
        StringBuilder builder = new StringBuilder();
        builder.append(variable);
        for (String s : presenceConditions) {
            builder.append(delim).append(s);
        }
        return builder.toString();
    }
    
    public String getVariable() {
        return variable;
    }

    @Override
    public String toCsvLine() {
        return toCsvLine(";");
    }

    @Override
    public String headertoCsvLine() {
        return headertoCsvLine(";");
    }

    @Override
    public String headertoCsvLine(String delim) {
        return "variable" + delim + "presence conditions";
    }

}
