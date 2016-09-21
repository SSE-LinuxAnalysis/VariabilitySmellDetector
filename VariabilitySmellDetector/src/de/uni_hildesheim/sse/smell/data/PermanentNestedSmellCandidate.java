package de.uni_hildesheim.sse.smell.data;

import java.util.Set;

import org.sat4j.specs.TimeoutException;

import de.uni_hildesheim.sse.smell.filter.AbstractSatSolverFilter;
import de.uni_hildesheim.sse.smell.util.VarNotFoundException;

public class PermanentNestedSmellCandidate extends NestedDependsSmellCandidate implements Comparable<PermanentNestedSmellCandidate> {

    private Set<String> permanentParents;
    private String permanentNestedVar;
    
    public PermanentNestedSmellCandidate(NestedDependsSmellCandidate other, Set<String> permanentParents, String permanentNestedVar) {
        super(other);
        this.permanentParents = permanentParents;
        this.permanentNestedVar = permanentNestedVar;
    }
    
    Set<String> getPermanentParents() {
        return permanentParents;
    }
    
    public String getPermanentNestedVar() {
        return permanentNestedVar;
    }
    
    @Override
    protected String getType() {
        return "permanent nesting depends candidate";
    }
    
    @Override
    public String toCsvLine(String delim) {
        return permanentNestedVar + delim + permanentParents.toString() + delim + super.toCsvLine(delim);
    }
    
    @Override
    public String headertoCsvLine(String delim) {
        return "Variable" + delim + "Permanent Parents" + delim + super.headertoCsvLine(delim);
    }
    
    @Override
    public ISmell getSmell(AbstractSatSolverFilter solver) {
        String solutionStr = "Error";
        try {
            solutionStr = getSolutionString(solver.getSolutionPart(getAllVariables()));
        } catch (VarNotFoundException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return new PermanentNestedSmell(this, solutionStr);
    }

    @Override
    public int compareTo(PermanentNestedSmellCandidate o) {
        return permanentNestedVar.compareTo(o.permanentNestedVar);
    }
}
