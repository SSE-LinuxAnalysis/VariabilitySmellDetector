package de.uni_hildesheim.sse.smell.filter.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariableWithSolutions;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;
import de.uni_hildesheim.sse.smell.util.dnf.Solution;

/**
 * <b>Input</b>: {@link VariableWithSolutions}s<br />
 * <b>Output</b>: {@link VariableWithSolutions}s<br />
 * 
 * @author Adam Krafczyk
 */
public class RedundantSolutionFilter implements IFilter {

    public RedundantSolutionFilter() {
    }
    
    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws FilterException {
        List<IDataElement> result = new ArrayList<IDataElement>();
        progressPrinter.start(this, data.size());
        
        
        for (IDataElement element : data) {
            if (!(element instanceof VariableWithSolutions)) {
                throw new WrongFilterException(VisibleVariableFilter.class, VariableWithSolutions.class, element);
            }
            VariableWithSolutions var = (VariableWithSolutions) element;
            
            List<Solution> solutions = var.getSolutions();
            
            boolean simplifiedOne;
            do {
                simplifiedOne = false;
                // try to find and simplify a pair
                for (int i = 0; i < solutions.size(); i++) {
                    simplifiedOne = removeAlmostEqualPartner(i, solutions);
                    if (simplifiedOne) {
                        break;
                    }
                }
                
                // remove equal solutions
                removeDuplicates(solutions);
                
            } while (simplifiedOne);
            
            result.add(var);
            
            progressPrinter.finishedOne();
        }
        
        return result;
    }
    
    private boolean removeAlmostEqualPartner(int first, List<Solution> solutions) {
        Solution s1 = solutions.get(first);
        
        for (int i = first + 1; i < solutions.size(); i++) {
            Solution s2 = solutions.get(i);
            
            if (s1.hasSameVariables(s2)) {
                Set<String> difference = s1.getDifference(s2);
                
                if (difference.size() == 1) {
                    s1.removeValue(difference.iterator().next());
                    solutions.remove(i);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private void removeDuplicates(List<Solution> solutions) {
        for (int i = 0; i < solutions.size(); i++) {
            for (int j = i + 1; j < solutions.size(); j++) {
                Solution s1 = solutions.get(i);
                Solution s2 = solutions.get(j);

//                System.out.println("Testing " + s1 + " & " + s2);
                if (s1.equals(s2)) {
                    solutions.remove(j);
                    j--;
                }
            }
        }
    }
    
}
