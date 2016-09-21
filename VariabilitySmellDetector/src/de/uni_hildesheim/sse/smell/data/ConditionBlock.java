package de.uni_hildesheim.sse.smell.data;

import java.util.Set;

import de.uni_hildesheim.sse.smell.util.ConditionUtils;

/**
 * A condition block as read from a preprocessor directive. When storing these
 * the order in which they appear should be kept, since it's harder to restore
 * the nesting information otherwise.
 * 
 * @author Adam Krafczyk
 */
public class ConditionBlock implements IDataElement {
    
    private static final String DEFAULT_DELIMETER = ";";
    
    private String filename;
    
    private int lineStart;
    
    private int lineEnd;
    
    private ConditionType type;
    
    private int indentation;
    
    private int startingIfLine;
    
    private String condition;
    
    private String normalized;
    
    /**
     * Creates a ConditionBlock with the given attributes.
     */
    public ConditionBlock(String filename, int lineStart, int lineEnd, ConditionType type,
            int indentation, int startingIfLine, String condition, String normalizedCondition) {
        this.filename = filename;
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.type = type;
        this.indentation = indentation;
        this.startingIfLine = startingIfLine;
        this.condition = condition;
        this.normalized = normalizedCondition;
    }

    /**
     * @return The filename that this condition block occurred in.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @return The line in the source file where this condition block started.
     */
    public int getLineStart() {
        return lineStart;
    }

    /**
     * @return The line in the source file where this condition block ended.
     */
    public int getLineEnd() {
        return lineEnd;
    }

    /**
     * @return The type of this condition block.
     */
    public ConditionType getType() {
        return type;
    }

    /**
     * @return The number of condition blocks that this condition block is nested in.
     */
    public int getIndentation() {
        return indentation;
    }

    /**
     * @return The line number of the if-Block that this condition block belongs to.
     *      For an if this is the same as {@link #getLineStart()}.
     *      For else or elseif this is the starting line of the if block that
     *      this condition block belongs to.
     */
    public int getStartingIfLine() {
        return startingIfLine;
    }

    /**
     * @return The condition of this block as read from the source file.
     *      This is empty for else blocks.
     */
    public String getCondition() {
        return condition;
    }

    /**
     * @return The normalized condition of this block. This resolves nesting and
     *      elseif / else relations. This condition is the presence condition
     *      of all source lines in this block, if there is not another block
     *      nested inside of this.
     */
    public String getNormalizedCondition() {
        return normalized;
    }
    
    @Override
    public String toCsvLine(String delim) {
        return filename + delim + lineStart + delim + lineEnd + delim + type
                + delim + indentation + delim + startingIfLine + delim
                + condition + delim + normalized;
    }
    
    @Override
    public String toCsvLine() {
        return toCsvLine(DEFAULT_DELIMETER);
    }
    
    /**
     * @return A set of all variables found in the condition.
     * @see #getCondition()
     */
    public Set<String> getVariablesInCondition() {
        return ConditionUtils.getVariables(condition);
    }
    
    /**
     * @return A set of all variables found in the normalized condition.
     * @see #getNormalizedCondition()
     */
    public Set<String> getVariablesInNormalizedCondition() {
        return ConditionUtils.getVariables(normalized);
    }

	@Override
	public String headertoCsvLine() {
		return headertoCsvLine(DEFAULT_DELIMETER);
	}

	@Override
	public String headertoCsvLine(String delim) {
		return "File" + delim + "Line Start" + delim + "Line End" + delim + "Type"
                + delim + "Nested Level" + delim + "Start of Block" + delim
                + "Condition" + delim + "Normalized Condition";
	}

}
