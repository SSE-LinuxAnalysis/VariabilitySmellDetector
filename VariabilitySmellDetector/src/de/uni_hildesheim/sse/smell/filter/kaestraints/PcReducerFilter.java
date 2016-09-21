package de.uni_hildesheim.sse.smell.filter.kaestraints;

import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariablePresenceConditions;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;
import de.uni_hildesheim.sse.smell.util.ConditionUtils;

/**
 * <b>Input</b>: {@link VariablePresenceConditions}s<br />
 * <b>Output</b>: {@link VariablePresenceConditions}s<br />
 * 
 * @author Adam Krafczyk
 */
public class PcReducerFilter implements IFilter {

    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws FilterException {
        List<IDataElement> result = new ArrayList<>();
        
        progressPrinter.start(this, data.size());
        
        for (IDataElement element : data) {

            if (!(element instanceof VariablePresenceConditions)) {
                throw new WrongFilterException(PcReducerFilter.class, VariablePresenceConditions.class, data);
            }
            VariablePresenceConditions pcs = (VariablePresenceConditions) element;
            
            List<String> reduced = ConditionUtils.reduceFormulas(pcs.getPresenceConditions());
         
            VariablePresenceConditions newPcs = new VariablePresenceConditions(pcs.getVariable());
            for (String s : reduced) {
                newPcs.addPresenceCondition(s);
            }
            
            result.add(newPcs);
            
            progressPrinter.finishedOne();
        }
        
        return result;
    }

}
