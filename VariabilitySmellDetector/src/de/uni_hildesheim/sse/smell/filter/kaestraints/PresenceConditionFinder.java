package de.uni_hildesheim.sse.smell.filter.kaestraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.ConditionBlock;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariablePresenceConditions;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;

/**
 * <b>Input</b>: {@link ConditionBlock}s<br />
 * <b>Output</b>: {@link VariablePresenceConditions}s<br />
 * 
 * @author Adam Krafczyk
 */
public class PresenceConditionFinder implements IFilter {

    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws FilterException {
        
        Map<String, VariablePresenceConditions> variablePresenceConditions = new HashMap<>(20000);
        
        progressPrinter.start(this, data.size());
        
        for (IDataElement element : data) {
            if (!(element instanceof ConditionBlock)) {
                throw new WrongFilterException(PresenceConditionFinder.class, ConditionBlock.class, element);
            }
            ConditionBlock block = (ConditionBlock) element;
            
            Set<String> vars = block.getVariablesInNormalizedCondition();
            
            for (String variable : vars) {
                VariablePresenceConditions vpc = variablePresenceConditions.get(variable);
                if (vpc == null) {
                    vpc = new VariablePresenceConditions(variable);
                    variablePresenceConditions.put(variable, vpc);
                }
                
                vpc.addPresenceCondition(block.getNormalizedCondition());
            }
            
            progressPrinter.finishedOne();
        }
        
        List<IDataElement> result = new ArrayList<IDataElement>();
        
        for (VariablePresenceConditions entry : variablePresenceConditions.values()) {
            
            if (!entry.getVariable().startsWith("CONFIG_")) {
//                System.out.println(entry.getVariable());
//                for (String pc : entry.getPresenceConditions()) {
//                    System.out.println("\t" + pc);
//                }
            } else {
                result.add(entry);
            }
            
        }
        
        return result;
    }

}
