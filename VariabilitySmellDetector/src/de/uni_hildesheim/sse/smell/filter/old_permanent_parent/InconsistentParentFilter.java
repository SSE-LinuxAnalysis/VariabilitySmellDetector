package de.uni_hildesheim.sse.smell.filter.old_permanent_parent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.PermanentNestedSmellCandidate;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;
import de.uni_hildesheim.sse.smell.util.ConditionUtils;
import de.uni_hildesheim.sse.smell.util.ConstraintException;
import de.uni_hildesheim.sse.smell.util.RecursiveReplacingCnfConverter;
import de.uni_hildesheim.sse.smell.util.VarNotFoundException;
import de.uni_hildesheim.sse.smell.util.VariableToNumberConverter;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.filter.DeclrationInConstraintFinder;

public class InconsistentParentFilter implements IFilter {

    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws WrongFilterException {
        
        progressPrinter.start("InconsistentParentFilter step 1 of 3", data.size());
        
        //  Var,    Conditions
        Map<String, List<String>> parentConditions = new HashMap<>();
        
        for (int i = 0; i < data.size(); i++) {
            if (!(data.get(i) instanceof PermanentNestedSmellCandidate)) {
                throw new WrongFilterException(InconsistentParentFilter.class, PermanentNestedSmellCandidate.class, data.get(i));
            }
            PermanentNestedSmellCandidate candidate = (PermanentNestedSmellCandidate) data.get(i);
            
            List<String> parents = parentConditions.get(candidate.getPermanentNestedVar());
            if (parents == null) {
                parents = new LinkedList<>();
                parentConditions.put(candidate.getPermanentNestedVar(), parents);
            }
            parents.add(candidate.getOuterCondition());
            
            progressPrinter.finishedOne();
        }
        
        progressPrinter.start("InconsistentParentFilter step 2 of 3", parentConditions.entrySet().size());
        
        Set<String> allowedVars = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : parentConditions.entrySet()) {
            String var = entry.getKey();
            List<String> conditions = entry.getValue();
            
            StringBuilder toCheck = new StringBuilder();
            toCheck.append("!(").append(conditions.get(0)).append(")");
            for (int i = 1; i < conditions.size(); i++) {
                toCheck.append(" && !(").append(conditions.get(i)).append(")");
            }
            
            try {
                if (isSolvable(toCheck.toString())) {
                    allowedVars.add(var);
                }
            } catch (ConstraintException e) {
                System.out.println("Can't parse expression: " + toCheck.toString());
            }
            
            progressPrinter.finishedOne();
        }
        
        progressPrinter.start("InconsistentParentFilter step 3 of 3", data.size());
        
        List<IDataElement> result = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            if (!(data.get(i) instanceof PermanentNestedSmellCandidate)) {
                throw new WrongFilterException(InconsistentParentFilter.class, PermanentNestedSmellCandidate.class, data.get(i));
            }
            PermanentNestedSmellCandidate candidate = (PermanentNestedSmellCandidate) data.get(i);
            
            if (allowedVars.contains(candidate.getPermanentNestedVar())) {
                result.add(candidate);
            }
            
            progressPrinter.finishedOne();
        }
        return result;
    }

    private boolean isSolvable(String expression) throws ConstraintException {
        ISolver solver = SolverFactory.newDefault();
        solver.setDBSimplificationAllowed(false);
        
        ConstraintSyntaxTree tree = ConditionUtils.parseCString(expression);
        VariableToNumberConverter varConverter = new VariableToNumberConverter();
        DeclrationInConstraintFinder finder = new DeclrationInConstraintFinder(tree);
        for (AbstractVariable var : finder.getDeclarations()) {
            varConverter.addVarible(var.getName());
        }
        
        List<ConstraintSyntaxTree> cnfParts = new RecursiveReplacingCnfConverter(varConverter).convertToCnf(tree);
        
        try {
            for (ConstraintSyntaxTree cst : cnfParts) {
                int[] numbers = varConverter.convertToDimacs(cst);
                solver.addClause(new VecInt(numbers));
            }
        } catch (ContradictionException e) {
            // This is thrown if the model is already not solvable
            return false;
        } catch (VarNotFoundException e) {
            // can't happen
            e.printStackTrace(System.out);
        }
        
        boolean result = false;
        try {
            result = solver.isSatisfiable();
        } catch (TimeoutException e) {
            e.printStackTrace(System.out);
        }
        return result;
    }
    
}
