package de.uni_hildesheim.sse.smell.util;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;

/**
 * Utility functions for merging constraints.<br/>
 * For instance:<br/>
 * <tt>(E and A) or (E and B)</tt>
 * should be merged to: <br/>
 * <tt>E and (A or B)</tt> 
 * @author El-Sharkawy
 *
 */
class ConstraintMerger {
    
    /**
     * Merges two constraints into one. Will not check whether this is suitable.
     * @param equalPart The equal part of both constraints, e.g., <tt>E</tt> in: <tt>(E and A) or (E and B)</tt>
     * @param mergeOperation The operation how the both constraints shall be connected, e.g.,
     *     <tt>or</tt> in: <tt>(E and A) or (E and B)</tt>
     * @param op The equal operations of both operations, e.g., <tt>and</tt> in: <tt>(E and A) or (E and B)</tt>
     * @param formula1 The first individual part, e.g., <tt>A</tt> in: <tt>(E and A) or (E and B)</tt>
     * @param formula2 The second individual part, e.g., <tt>B</tt> in: <tt>(E and A) or (E and B)</tt>
     * @return The merged constraint, e.g. <tt>E and (A or B)</tt> 
     */
    private static ConstraintSyntaxTree merge(ConstraintSyntaxTree equalPart, String mergeOperation, String op,
        ConstraintSyntaxTree formula1, ConstraintSyntaxTree formula2) {
        
        OCLFeatureCall innerPart = new OCLFeatureCall(formula1, mergeOperation, formula2);
        OCLFeatureCall outerPart = new OCLFeatureCall(equalPart, op, innerPart);
        
        return outerPart;
    }
    
    /**
     * Checks whether the two constraints can be combined and merges them if possible.
     * @param formula1 The first constraint to be merged
     * @param formula2 The second constraint to be merged
     * @param mergeOperation The operation how the both constraints shall be connected, e.g.,
     *     <tt>or</tt> in: <tt>(E and A) or (E and B)</tt>
     * @return The merged constraint if merging was possible, or <tt>null</tt>
     */
    static ConstraintSyntaxTree merge(ConstraintSyntaxTree formula1, ConstraintSyntaxTree formula2,
        String mergeOperation) {
        
        ConstraintSyntaxTree result = null;
        
        if (formula1 instanceof OCLFeatureCall && formula2 instanceof OCLFeatureCall
            && ((OCLFeatureCall) formula1).getOperation().equals(((OCLFeatureCall) formula2).getOperation())
            && ((OCLFeatureCall) formula1).getParameterCount() == 1) {
            
            OCLFeatureCall call1 = (OCLFeatureCall) formula1;
            OCLFeatureCall call2 = (OCLFeatureCall) formula2;
            String op = call1.getOperation();
            
            ConstraintSyntaxTree operand1 = call1.getOperand();
            ConstraintSyntaxTree operand2 = call2.getOperand();
            ConstraintSyntaxTree param1 = call1.getParameter(0);
            ConstraintSyntaxTree param2 = call2.getParameter(0);
            
            // TODO: equality is checked based on String representation -> Could be improved
            if (operand1.toString().equals(operand2.toString())) {
                result = merge(operand1, mergeOperation, op, param1, param2);
            } else if (operand1.toString().equals(param2.toString())) {
                result = merge(operand1, mergeOperation, op, param1, operand2);
            } else if (param1.toString().equals(operand2.toString())) {
                result = merge(param1, mergeOperation, op, operand1, param2);
            } else if (param1.toString().equals(param2.toString())) {
                result = merge(param1, mergeOperation, op, operand1, operand2);
            }
        }
        
        return result;
    }
    
    /**
     * Filters and merges the given constraints with an greedy algorithm
     * @param formulas Formulas which belongs together and maybe merged to one big constraint.
     * @param mergeOperation The operation how the both constraints shall be connected, e.g.,
     *     <tt>or</tt> in: <tt>(E and A) or (E and B)</tt>
     * @return The parsed, filtered, and merged constraints.
     */
    static List<ConstraintSyntaxTree> mergeConstraints(List<String> formulas, String mergeOperation) {
        List<ConstraintSyntaxTree> results = new ArrayList<>();
        formulas = ConditionUtils.filterDuplicates(formulas);
        
        for (int i = 0; i < formulas.size(); i++) {
            try {
                ConstraintSyntaxTree parsedFormula = ConditionUtils.parseCString(formulas.get(i));
                ConstraintSyntaxTree merged = null;
                
                for (int j = 0; j < results.size() && merged == null; j++) {
                    merged = merge(results.get(j), parsedFormula, mergeOperation);
                    if (merged != null) {
                        results.set(j, merged);
                    }
                }
                
                if (null == merged) {
                    results.add(parsedFormula);
                }
                
            } catch (ConstraintException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return results;
    }
}
