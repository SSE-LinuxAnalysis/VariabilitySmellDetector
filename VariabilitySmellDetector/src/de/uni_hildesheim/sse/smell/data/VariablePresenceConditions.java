package de.uni_hildesheim.sse.smell.data;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VariablePresenceConditions implements IDataElement {
    
    private String variable;
    
    private Set<String> presenceConditions;
    
    public VariablePresenceConditions(String variable) {
        this.variable = variable;
        this.presenceConditions = new HashSet<>(10000);
    }
    
    public void addPresenceCondition(String pc) {
        presenceConditions.add(pc);
    }
    
    public int getNumPresenceConditions() {
        return presenceConditions.size();
    }
    
    public List<String> getPresenceConditions() {
        return new ArrayList<>(presenceConditions);
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
    
    public void printCsvLine(PrintWriter out) {
        out.print(variable);
        for (String s : presenceConditions) {
            out.print(";");
            out.print(s);
        }
        out.println();
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
