package de.uni_hildesheim.sse.smell.util;

import java.util.List;

import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;

/**
 * Introduces new variables to simplify overall constraint.
 * Resulting constraints are equisatisfiable.
 * 
 * Reduces the number of resulting constraints from (worst case) exponential growth to
 * quadratic growth.
 * 
 * @author Adam Krafczyk
 */
public class RecursiveReplacingCnfConverter extends RecursiveCnfConverter {
    
    private static int UNIQUE = 1;
    
    private VariableToNumberConverter varConverter;
    
    /**
     * For test cases.
     */
    public RecursiveReplacingCnfConverter() {
    }
    
    /**
     * Default constructor.
     * @param varConverter A {@link VariableToNumberConverter} that we tell about new variables.
     */
    public RecursiveReplacingCnfConverter(VariableToNumberConverter varConverter) {
        this.varConverter = varConverter;
    }
    
    @Override
    protected void handleOr(OCLFeatureCall call, List<ConstraintSyntaxTree> result) throws ConstraintException {
        /*
         * We have call = P v Q
         * 
         * If P and Q both are "complex", then we do the following to simplify the constraint:
         * 
         *     We introduce a new variable, Z, such that (~Z v P) ^ (Z v Q).
         *     This is satisfiable if, and only if, call is satisfiable.
         *     
         * A formula is complex, if it contains more than one variable.
         */
        
        if (isComplex(call.getOperand()) && isComplex(call.getParameter(0))) {
            DecisionVariableDeclaration decl = new DecisionVariableDeclaration("temp_" + (UNIQUE++), BooleanType.TYPE, null);
            
            if (varConverter != null) {
                // "reserve" new number for newly introduced number
                varConverter.addVarible(decl.getName());
            }
            
            Variable z = new Variable(decl);
            OCLFeatureCall notZ = new OCLFeatureCall(z, OclKeyWords.NOT);
            
            OCLFeatureCall left = new OCLFeatureCall(notZ, OclKeyWords.OR, call.getOperand());
            OCLFeatureCall right = new OCLFeatureCall(z, OclKeyWords.OR, call.getParameter(0));
            
            result.addAll(convertPrivate(new OCLFeatureCall(left, OclKeyWords.AND, right)));
        } else {
            super.handleOr(call, result);
        }
        
    }
    
    /**
     * A formula is complex, if it contains more than one variable.
     * @param tree
     * @return <tt>true</tt> if tree contains more than one variable
     * @throws ConstraintException
     */
    private boolean isComplex(ConstraintSyntaxTree tree) throws ConstraintException {
        if (tree instanceof Variable) {
            return false;
        } else if (tree instanceof OCLFeatureCall) {
            OCLFeatureCall call = (OCLFeatureCall) tree;
            
            if (call.getOperation().equals(OclKeyWords.NOT)) {
                return isComplex(call.getOperand());
            } else {
                return true;
            }
            
        } else {
            throw new ConstraintException("Invalid element in tree: " + tree);
        }
    }

}
