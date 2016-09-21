package de.uni_hildesheim.sse.smell.filter.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.ConditionBlock;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;

/**
 * A filter that filters {@link ConditionBlock}s for <code>CONFIG_</code> variables.
 * <br />
 * <b>Input</b>: {@link ConditionBlock}s<br />
 * <b>Output</b>: {@link ConditionBlock}s<br />
 * 
 * @author Adam Krafczyk
 */
public class ConfigVarFilter implements IFilter {
    
    private boolean configOnly;
    
    private boolean mixed;
    
    private boolean noConfigs;
    
    /**
     * Creates a new filter that will return the specified types of constrains. 
     * 
     * @param onlyConfig Whether the filter will let constraints that only contain
     *      <code>CONFIG_</code> variables pass through.
     * @param mixed Whether the filter will let constraints that contain
     *      <code>CONFIG_</code> and other variables pass through.
     * @param noConfigs Whether the filter will let constraints that contain no
     *      <code>CONFIG_</code> variables pass through.
     */
    public ConfigVarFilter(boolean onlyConfig, boolean mixed, boolean noConfigs) {
        this.configOnly = onlyConfig;
        this.mixed = mixed;
        this.noConfigs = noConfigs;
    }
    
    @Override
    public List<IDataElement> run(List<IDataElement> elements, IProgressPrinter progressPrinter) throws WrongFilterException {
        List<IDataElement> result = new ArrayList<IDataElement>();
        
        progressPrinter.start(this, elements.size());
        
        for (IDataElement element : elements) {
            if (!(element instanceof ConditionBlock)) {
                throw new WrongFilterException(ConfigVarFilter.class, ConditionBlock.class, element);
            }
            ConditionBlock block = (ConditionBlock) element;
            
            Set<String> vars = block.getVariablesInNormalizedCondition();
            
            boolean containsOther = false;
            boolean containsConfig = false;
            
            for (String var : vars) {
                if (var.startsWith("CONFIG_")) { // TODO: ENABLE_ for busybox
                    containsConfig = true;
                } else {
                    containsOther = true;
                }
                if (containsConfig && containsOther) {
                    break;
                }
            }
            
            if (containsConfig && containsOther) {
                if (this.mixed) {
                    result.add(block);
                }
            } else if (containsConfig) {
                if (this.configOnly) {
                    result.add(block);
                }
            } else {
                if (this.noConfigs) {
                    result.add(block);
                }
            }
            
            progressPrinter.finishedOne();
        }
        
        return result;
    }
    
}
