package de.uni_hildesheim.sse.smell.data;

import java.util.Map;
import java.util.Set;

import org.sat4j.specs.TimeoutException;

import de.uni_hildesheim.sse.smell.filter.AbstractSatSolverFilter;
import de.uni_hildesheim.sse.smell.util.ConditionUtils;
import de.uni_hildesheim.sse.smell.util.VarNotFoundException;

/**
 * A possible smell, where the implied dependency of nested condition blocks
 * collides with the VarModel.
 * 
 * @author Adam Krafczyk
 */
public class NestedDependsSmellCandidate implements ISmellCandidate {
    
    private static final int INNER = 0;
    private static final int OUTER = 1;
    private static final int FILE = 2;
    private static final int INNER_LINE = 3;
    private static final int OUTER_LINE = 4;

    private static final String DEFAULT_DELIMETER = ";";
    
    private String innerCondition;
    
    private String outerCondition;
    
    private String file;
    
    private int innerLine;
    
    private int outerLine;
    
    /**
     * To be used by the {@link NestedDependsSmell} to initialize attributes.
     */
    protected NestedDependsSmellCandidate(NestedDependsSmellCandidate other) {
        this.innerCondition = other.innerCondition;
        this.outerCondition = other.outerCondition;
        this.file = other.file;
        this.innerLine = other.innerLine;
        this.outerLine = other.outerLine;
    }
    
    /**
     * Creates a smell candidate for the given inner and outer condition blocks.
     * 
     * @param inner The condition block that is nested inside the outer block.
     * @param outer The outer condition block.
     */
    public NestedDependsSmellCandidate(ConditionBlock inner, ConditionBlock outer) {
        this.innerCondition = inner.getCondition();
        this.outerCondition = outer.getNormalizedCondition();
        this.file = inner.getFilename();
        this.innerLine = inner.getLineStart();
        this.outerLine = outer.getLineStart();
    }
    
    /**
     * Reads the smell candidate from the given CSV line.
     * 
     * @param csvLine The line to be parsed into a smell candidate. Format:
     *      <code>innerCondition; outerCondition; filename; innerLine; outerLine</code>
     * @param delim The delimiter of the CSV line.
     */
    public NestedDependsSmellCandidate(String csvLine, String delim) {
        String[] parts = csvLine.split(delim);
        this.innerCondition = parts[INNER];
        this.outerCondition = parts[OUTER];
        this.file = parts[FILE];
        this.innerLine = Integer.parseInt(parts[INNER_LINE]);
        this.outerLine = Integer.parseInt(parts[OUTER_LINE]);
    }
    
    /**
     * Reads the smell candidate from the given CSV line.
     * 
     * @param csvLine The line to be parsed into a smell candidate with the
     *      default delimiter. See {@link #SimpleNestingSmellCandidate(String, String)}
     *      for format.
     * @param delim The delimiter of the CSV line.
     */
    public NestedDependsSmellCandidate(String csvLine) {
        this(csvLine, DEFAULT_DELIMETER);
    }
    
    /**
     * @return The condition of the block that is nested inside the outer block.
     */
    public String getInnerCondition() {
        return innerCondition;
    }
    
    /**
     * @return The normalized condition of the outer block.
     */
    public String getOuterCondition() {
        return outerCondition;
    }
    
    /**
     * @return The filename that the blocks are in.
     */
    public String getFile() {
        return file;
    }
    
    /**
     * @return The line number of the inner block in the source file.
     */
    public int getInnerLine() {
        return innerLine;
    }
    
    /**
     * @return The line number of the outer block in the source file.
     */
    public int getOuterLine() {
        return outerLine;
    }
    
    @Override
    public String toCsvLine() {
        return toCsvLine(DEFAULT_DELIMETER);
    }
    
    protected String getType() {
        return "nesting depends candidate";
    }
    
    @Override
    public String toCsvLine(String delim) {
        return getType() + delim + file + delim + innerCondition + delim
                + outerCondition + delim + innerLine + delim + outerLine;
    }
    
    /**
     * @return A set of all variables found in the inner and outer condition.
     */
    protected Set<String> getAllVariables() {
        Set<String> vars = ConditionUtils.getVariables(innerCondition);
        vars.addAll(ConditionUtils.getVariables(outerCondition));
        return vars;
    }
    
    @Override
    public String headertoCsvLine() {
        return headertoCsvLine(DEFAULT_DELIMETER);
    }

    @Override
    public String headertoCsvLine(String delim) {
        return "Type" + delim + "File" + delim + "Inner Cond." + delim + "Depends on"
            + delim + "Inner No." + delim + "Depends on No.";
    }

    @Override
    public String getVarModelCondition() {
        // !(inner -> outer)
        // <=> !(outer || !inner)
        // <=> !outer && inner
        return "!(" + outerCondition + ") && (" + innerCondition + ")";
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
        return new NestedDependsSmell(this, solutionStr);
    }
    
    /**
     * @param solution The part of a solution of the VarModel that lead to a smell.
     * @return The part of the solution as a String
     */
    protected static String getSolutionString(Map<String, Boolean> solution) {
        StringBuilder solutionStr = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : solution.entrySet()) {
            if (!entry.getValue()) {
                solutionStr.append("!");
            }
            solutionStr.append(entry.getKey());
            solutionStr.append(" ");
        }
        
        return solutionStr.toString();
    }
    
    @Override
    public String toString() {
        return toCsvLine();
    }
}
