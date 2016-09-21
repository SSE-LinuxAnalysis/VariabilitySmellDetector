package de.uni_hildesheim.sse.smell.filter.old_permanent_parent;

import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.ConditionBlock;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.NestedDependsSmellCandidate;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;

/**
 * A filter that locates nested condition blocks.
 * <br />
 * <b>Input</b>: {@link ConditionBlock}s<br />
 * <b>Output</b>: {@link NestedDependsSmellCandidate}s<br />
 * 
 * @author Adam Krafczyk
 * @author El-Sharkawy
 */
public class NestedDependsFinder implements IFilter {

    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws WrongFilterException {
        
        progressPrinter.start(this, data.size());
        
        List<IDataElement> result = new ArrayList<>();
        
        for (int i = 0; i < data.size(); i++) {
            if (!(data.get(i) instanceof ConditionBlock)) {
                throw new WrongFilterException(NestedDependsFinder.class, ConditionBlock.class, data.get(i));
            }
            
            ConditionBlock block = (ConditionBlock) data.get(i);
            
            // This skips else blocks
            if (block.getVariablesInCondition().size() < 1) {
                progressPrinter.finishedOne();
                continue;
            }
            
            if (block.getIndentation() > 0) {
                // Search parent
                ConditionBlock parent = null;
                for (int j = i - 1; j >= 0; j--) {
                    ConditionBlock other = (ConditionBlock) data.get(j);
                    if (other.getIndentation() == block.getIndentation() - 1) {
                        parent = other;
                        break;
                    }
                }
                
                if (parent != null && parent.getVariablesInNormalizedCondition().size() < 1) {
                    progressPrinter.finishedOne();
                    continue;
                }
                
                // Check whether current if has an else
                boolean isCandidate = hasNoClosingElse(data, i, block);
                
                NestedDependsSmellCandidate candidate = new NestedDependsSmellCandidate(block, parent);
                if (isCandidate) {
                    result.add(candidate);
                } else {
//                    StringBuffer debugMsg = new StringBuffer("No smell in +\"");
//                    debugMsg.append(candidate.getFile());
//                    debugMsg.append(": ");
//                    debugMsg.append(candidate.getOuterLine());
//                    debugMsg.append("+");
//                    debugMsg.append(candidate.getInnerLine());
//                    debugMsg.append("\" inner cond. \"");
//                    debugMsg.append(candidate.getInnerCondition());
//                    debugMsg.append("\" depends on \"");
//                    debugMsg.append(candidate.getOuterCondition());
//                    debugMsg.append("\".");
//                    System.err.println(debugMsg.toString());
                }
            }
            progressPrinter.finishedOne();
        }
        
        return result;
    }

    /**
     * Checks whether the current condition block has a closing else block.
     * @param data The list of condition blocks.
     * @param line The current index of the data, which is tested.
     * @param block The current element, which is tested (<code>data[i]</code>).
     * @return <tt>true</tt> the given <tt>block</tt> has no closing else, <tt>false</tt> otherwise.
     */
    private boolean hasNoClosingElse(List<IDataElement> data, int line, ConditionBlock block) {
        boolean isCandidate = true;
        for (int j = line + 1; j < data.size() && isCandidate; j++) {
            ConditionBlock other = (ConditionBlock) data.get(j);
            if (other.getIndentation() == block.getIndentation()) {
                if ("else".equals(other.getType())) {
                    // It is not a smell candidate, if an else on the same indentation layer is found
                    isCandidate = false;
                } else if ("if".equals(other.getType())) {
                    // Abort if the next if is found on the same indentation layer
                    break;
                }
            } else if (other.getIndentation() <= block.getIndentation()) {
                // Abort, if we leave the nested layer (go up)
                break;
            }
        }
        return isCandidate;
    }

}
