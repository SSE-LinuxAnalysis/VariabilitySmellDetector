package de.uni_hildesheim.sse.smell.data;

/**
 * A smell derived from a {@link NestedDependsSmellCandidate}.
 * 
 * @author Adam Krafczyk
 */
public class NestedDependsSmell extends NestedDependsSmellCandidate implements ISmell {

    private String solution;
    
    /**
     * Creates a new smell for the given candidate.
     * @param candidate The smell candidate that turned out to be an actual smell.
     * @param solution The relevant part of the solution of the VarModel that lead
     *      to this smell.
     */
    public NestedDependsSmell(NestedDependsSmellCandidate candidate, String solution) {
        super(candidate);
        this.solution = solution;
    }
    
    /**
     * @return The relevant part of the solution of the VarModel that lead
     *      to this smell.
     */
    public String getSolution() {
        return solution;
    }
    
    @Override
    protected String getType() {
        return "nesting depends";
    }
    
    @Override
    public String toCsvLine(String delim) {
        String solution = this.solution.replace(" ", ", ");
        if (solution.endsWith(", ")) {
            solution = solution.substring(0, solution.length() - 3);
        }
        return super.toCsvLine(delim) + delim + solution;
    }
    
    @Override
    public String headertoCsvLine(String delim) {
        return super.headertoCsvLine(delim) + delim + "Excluded Condition";
    }
    
}
