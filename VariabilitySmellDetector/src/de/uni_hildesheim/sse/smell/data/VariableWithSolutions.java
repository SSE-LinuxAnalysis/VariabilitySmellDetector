package de.uni_hildesheim.sse.smell.data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.uni_hildesheim.sse.smell.util.dnf.Solution;

public class VariableWithSolutions implements IDataElement {
    
    private String variable;
    
    private List<Solution> solutions;
    
    public VariableWithSolutions(String variable) {
        this.variable = variable;
        this.solutions = new LinkedList<>();
    }
    
    public void addSolutions(Collection<Solution> solutions) {
        this.solutions.addAll(solutions);
    }
    
    public void addSolution(Solution solution) {
        solutions.add(solution);
    }
    
    public List<Solution> getSolutions() {
        return solutions;
    }
    
    @Override
    public String toCsvLine(String delim) {
        StringBuilder builder = new StringBuilder();
        builder.append(variable);
        for (Solution s : solutions) {
            builder.append(delim).append(s.toString());
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
        return "variable" + delim + "solutions";
    }

}
