package de.uni_hildesheim.sse.smell.util.dnf;

import java.util.HashSet;
import java.util.Set;

import de.uni_hildesheim.sse.smell.util.ConstraintException;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;

public class SolutionFinder {

    private Variable[] variables;
    
    public Set<Solution> getSolutions(ConstraintSyntaxTree formula) throws ConstraintException {
        CstToFormulaConverter converter = new CstToFormulaConverter();
        try {
            formula.accept(converter);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ConstraintException) {
                throw (ConstraintException) e.getCause();
            }
            throw e;
        }
        variables = converter.getVariables();
        return getSolutions(converter.getResult());
    }
    
    private Set<Solution> getSolutions(Formula f) throws ConstraintException {
        Set<Solution> solutions = new HashSet<>();
        
        do {
            if (f.evaluate()) {
                Solution newSolution = new Solution();
                
                for (int i = 0; i < variables.length; i++) {
                    newSolution.setValue(variables[i].getName(), variables[i].evaluate());
                }
                
                solutions.add(newSolution);
            }
            
            
        } while (setNextCombination());
        
        return solutions;
    }
    
    private boolean setNextCombination() {
        boolean allAreFalse = true;
        for (int i = 0; i < variables.length; i++) {
            variables[i].setValue(!variables[i].evaluate());
            if (variables[i].evaluate()) {
                allAreFalse = false;
                break;
            }
        }
        
        return !allAreFalse;
    }
    
}
