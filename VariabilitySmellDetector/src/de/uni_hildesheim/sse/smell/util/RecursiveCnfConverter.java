package de.uni_hildesheim.sse.smell.util;

import java.util.LinkedList;
import java.util.List;

import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.values.BooleanValue;

/**
 * A CNF converter based on https://www.cs.jhu.edu/~jason/tutorials/convert-to-CNF.html
 * 
 * @author Adam Krafczyk
 */
public class RecursiveCnfConverter implements ICnfConverter {
    
    public static final Variable PSEUDO_TRUE = new Variable(
            new DecisionVariableDeclaration("PSEUDO_TRUE", BooleanType.TYPE, null));
    public static final Variable PSEUDO_FALSE = new Variable(
            new DecisionVariableDeclaration("PSEUDO_FALSE", BooleanType.TYPE, null));
    
    @Override
    public List<ConstraintSyntaxTree> convertToCnf(ConstraintSyntaxTree tree) throws ConstraintException {
        List<ConstraintSyntaxTree> result = new LinkedList<>();
        
        if (containsConstants(tree)) {
            result.add(PSEUDO_TRUE);
            result.add(new OCLFeatureCall(PSEUDO_FALSE, OclKeyWords.NOT));
            
            tree = replaceConstants(tree);
        }
        
        result.addAll(convertPrivate(tree));
        
        return result;
    }
    
    private static boolean containsConstants(ConstraintSyntaxTree tree) {
        if (tree instanceof ConstantValue) {
            return true;
            
        } else if (tree instanceof OCLFeatureCall) {
            OCLFeatureCall call = (OCLFeatureCall) tree;
            
            boolean containsConstant = false;
            
            containsConstant |= containsConstants(call.getOperand());
            for (int i = 0; i < call.getParameterCount(); i++) {
                containsConstant |= containsConstants(call.getParameter(i));
            }
            
            return containsConstant;
            
        } else {
            return false;
        }
    }
    
    private static ConstraintSyntaxTree replaceConstants(ConstraintSyntaxTree tree) throws ConstraintException {
        
        if (tree instanceof ConstantValue) {
            ConstantValue value = (ConstantValue) tree;
            if (value.getConstantValue() instanceof BooleanValue) {
                
                if (((BooleanValue) value.getConstantValue()).getValue()) {
                    return PSEUDO_TRUE;
                } else {
                    return PSEUDO_FALSE;
                }
                
            } else {
                throw new ConstraintException("Invalid constant value");
            }
            
        } else if (tree instanceof OCLFeatureCall) {
            OCLFeatureCall call = (OCLFeatureCall) tree;
            ConstraintSyntaxTree operand = replaceConstants(call.getOperand());
            ConstraintSyntaxTree[] parameters = new ConstraintSyntaxTree[call.getParameterCount()];
            for (int i = 0; i < call.getParameterCount(); i++) {
                parameters[i] = replaceConstants(call.getParameter(i));
            }
            
            return new OCLFeatureCall(operand, call.getOperation(), parameters);
            
        } else {
            return tree;
        }
        
    }
    
    protected final List<ConstraintSyntaxTree> convertPrivate(ConstraintSyntaxTree tree) throws ConstraintException {
        /*
         * See https://www.cs.jhu.edu/~jason/tutorials/convert-to-CNF.html
         */
        List<ConstraintSyntaxTree> result = new LinkedList<>();
        
        if (tree instanceof Variable) {
            handleVariable((Variable) tree, result);
            
        } else if (tree instanceof OCLFeatureCall) {
            OCLFeatureCall call = (OCLFeatureCall) tree;
            
            if (call.getOperation().equals(OclKeyWords.OR)) {
                handleOr(call, result);
                
            } else if (call.getOperation().equals(OclKeyWords.AND)) {
                handleAnd(call, result);
                
            } else if (call.getOperation().equals(OclKeyWords.NOT)) {
                handleNot(call, result);
                
            } else {
                throw new ConstraintException("Invalid operation in tree: " + call.getOperation());
            }
            
        } else {
            throw new ConstraintException("Invalid element in tree: " + tree);
        }
        
        return result;
    }
    
    protected void handleVariable(Variable var, List<ConstraintSyntaxTree> result) throws ConstraintException {
        result.add(var);
    }
    
    protected void handleOr(OCLFeatureCall call, List<ConstraintSyntaxTree> result) throws ConstraintException {
        /*
         * We have call = P v Q
         * 
         * CONVERT(P) must have the form P1 ^ P2 ^ ... ^ Pm, and
         * CONVERT(Q) must have the form Q1 ^ Q2 ^ ... ^ Qn,
         * where all the Pi and Qi are dijunctions of literals.
         * So we need a CNF formula equivalent to
         *    (P1 ^ P2 ^ ... ^ Pm) v (Q1 ^ Q2 ^ ... ^ Qn).
         * So return (P1 v Q1) ^ (P1 v Q2) ^ ... ^ (P1 v Qn)
         *         ^ (P2 v Q1) ^ (P2 v Q2) ^ ... ^ (P2 v Qn)
         *           ...
         *         ^ (Pm v Q1) ^ (Pm v Q2) ^ ... ^ (Pm v Qn)
         */
        List<ConstraintSyntaxTree> leftSide = convertPrivate(call.getOperand());
        List<ConstraintSyntaxTree> rightSide = convertPrivate(call.getParameter(0));
        
        for (ConstraintSyntaxTree p : leftSide) {
            for (ConstraintSyntaxTree q : rightSide) {
                OCLFeatureCall newOr = new OCLFeatureCall(p, OclKeyWords.OR, q);
                result.add(newOr);
            }
        }
//        result.addAll(ConditionUtils.getMaxTerms(call));
    }
    
    protected void handleAnd(OCLFeatureCall call, List<ConstraintSyntaxTree> result) throws ConstraintException {
        /*
         * We have call = P ^ Q
         * 
         * CONVERT(P) must have the form P1 ^ P2 ^ ... ^ Pm, and
         * CONVERT(Q) must have the form Q1 ^ Q2 ^ ... ^ Qn,
         * where all the Pi and Qi are disjunctions of literals.
         * So return P1 ^ P2 ^ ... ^ Pm ^ Q1 ^ Q2 ^ ... ^ Qn.
         */
        
        List<ConstraintSyntaxTree> leftSide = convertPrivate(call.getOperand());
        List<ConstraintSyntaxTree> rightSide = convertPrivate(call.getParameter(0));
        
        result.addAll(leftSide);
        result.addAll(rightSide);
    }
    
    protected void handleNot(OCLFeatureCall call, List<ConstraintSyntaxTree> result) throws ConstraintException {
        // If call has the form ~A for some variable A, then return call.
        if (call.getOperand() instanceof Variable) {
            result.add(call);
            
        } else if (call.getOperand() instanceof OCLFeatureCall) {
            OCLFeatureCall innerCall = (OCLFeatureCall) call.getOperand();
            
            // If call has the form ~(~P), then return CONVERT(P). (double negation)
            if (innerCall.getOperation().equals(OclKeyWords.NOT)) {
                result.addAll(convertPrivate(innerCall.getOperand()));
                
            // If call has the form ~(P ^ Q), then return CONVERT(~P v ~Q). (de Morgan's Law)
            } else if (innerCall.getOperation().equals(OclKeyWords.AND)) {
                ConstraintSyntaxTree p = innerCall.getOperand();
                ConstraintSyntaxTree q = innerCall.getParameter(0);
                
                OCLFeatureCall notP = new OCLFeatureCall(p, OclKeyWords.NOT);
                OCLFeatureCall notQ = new OCLFeatureCall(q, OclKeyWords.NOT);
                OCLFeatureCall newOr = new OCLFeatureCall(notP, OclKeyWords.OR, notQ);
                
                result.addAll(convertPrivate(newOr));
                
            // If call has the form ~(P v Q), then return CONVERT(~P ^ ~Q). (de Morgan's Law)
            } else if (innerCall.getOperation().equals(OclKeyWords.OR)) {
                ConstraintSyntaxTree p = innerCall.getOperand();
                ConstraintSyntaxTree q = innerCall.getParameter(0);
                
                OCLFeatureCall notP = new OCLFeatureCall(p, OclKeyWords.NOT);
                OCLFeatureCall notQ = new OCLFeatureCall(q, OclKeyWords.NOT);
                OCLFeatureCall newAnd = new OCLFeatureCall(notP, OclKeyWords.AND, notQ);
                
                result.addAll(convertPrivate(newAnd));
                
            } else {
                throw new ConstraintException("Invalid operation in tree: " + innerCall.getOperation());
            }
            
        } else {
            throw new ConstraintException("Invalid element in not call: " + call);
        }
    }

}
