package de.uni_hildesheim.sse.smell.data;

import java.util.Set;

public class PermanentNestedSmell extends NestedDependsSmell {
    
    private Set<String> permanentParents;
    private String permanentNestedVar;
    
    PermanentNestedSmell(PermanentNestedSmellCandidate candidate, String solution) {
        super(candidate, solution);
        this.permanentNestedVar = candidate.getPermanentNestedVar();
        this.permanentParents = candidate.getPermanentParents();
    }
    
    @Override
    public String toCsvLine(String delim) {
        return super.toCsvLine(delim) + delim + permanentNestedVar + delim + permanentParents.toString();
    }
    
    @Override
    public String headertoCsvLine(String delim) {
        return super.headertoCsvLine(delim) + delim + "Variable" + delim + "Permanent Parents";
    }
    
    @Override
    protected String getType() {
        return "permanent nesting depends";
    }

}
