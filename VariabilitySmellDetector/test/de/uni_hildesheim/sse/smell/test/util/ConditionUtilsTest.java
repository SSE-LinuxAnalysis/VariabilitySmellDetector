package de.uni_hildesheim.sse.smell.test.util;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.smell.util.ConditionUtils;
import de.uni_hildesheim.sse.smell.util.ConstraintException;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;

public class ConditionUtilsTest {

    @Test
    public void testParseCStringSimpleVariable() throws ConstraintException {
        String formula = "SOME_varIable";
        
        ConstraintSyntaxTree tree = ConditionUtils.parseCString(formula);
        
        checkVariable(tree, "SOME_varIable");
    }
    
    @Test
    public void testParseCStringSimpleOr() throws ConstraintException {
        String formula = "A || B";
        
        ConstraintSyntaxTree tree = ConditionUtils.parseCString(formula);
        
        OCLFeatureCall call = checkOclFeatureCall(tree, OclKeyWords.OR);
        checkVariable(call.getOperand(), "A");
        checkVariable(call.getParameter(0), "B");
    }
    
    @Test
    public void testParseCStringSimpleAnd() throws ConstraintException {
        String formula = "A && B";
        
        ConstraintSyntaxTree tree = ConditionUtils.parseCString(formula);
        
        OCLFeatureCall call = checkOclFeatureCall(tree, OclKeyWords.AND);
        checkVariable(call.getOperand(), "A");
        checkVariable(call.getParameter(0), "B");
    }
    
    @Test
    public void testParseCStringNestedNotInOr() throws ConstraintException {
        String formula = "A || !B";
        
        ConstraintSyntaxTree tree = ConditionUtils.parseCString(formula);
        
        OCLFeatureCall call = checkOclFeatureCall(tree, OclKeyWords.OR);
        checkVariable(call.getOperand(), "A");
        
        OCLFeatureCall notCall = checkOclFeatureCall(call.getParameter(0), OclKeyWords.NOT);
        checkVariable(notCall.getOperand(), "B");
    }
    
    @Test
    public void testParseCStringNestedNotInAnd() throws ConstraintException {
        String formula = "!A && B";
        
        ConstraintSyntaxTree tree = ConditionUtils.parseCString(formula);
        
        OCLFeatureCall call = checkOclFeatureCall(tree, OclKeyWords.AND);
        checkVariable(call.getParameter(0), "B");
        
        OCLFeatureCall notCall = checkOclFeatureCall(call.getOperand(), OclKeyWords.NOT);
        checkVariable(notCall.getOperand(), "A");
    }
    
    @Test
    public void testParseCStringParenthesis1() throws ConstraintException {
        String formula = "(!(A) && (B))";
        
        ConstraintSyntaxTree tree = ConditionUtils.parseCString(formula);
        
        OCLFeatureCall call = checkOclFeatureCall(tree, OclKeyWords.AND);
        checkVariable(call.getParameter(0), "B");
        
        OCLFeatureCall notCall = checkOclFeatureCall(call.getOperand(), OclKeyWords.NOT);
        checkVariable(notCall.getOperand(), "A");
    }
    
    @Test
    public void testParseCStringParenthesis2() throws ConstraintException {
        String formula = "(A || !(B))";
        
        ConstraintSyntaxTree tree = ConditionUtils.parseCString(formula);
        
        OCLFeatureCall call = checkOclFeatureCall(tree, OclKeyWords.OR);
        checkVariable(call.getOperand(), "A");
        
        OCLFeatureCall notCall = checkOclFeatureCall(call.getParameter(0), OclKeyWords.NOT);
        checkVariable(notCall.getOperand(), "B");
    }
    
    @Test
    public void testParseCStringComplex() throws ConstraintException {
        String formula = "A || !(B && ((C || !D) || (E && A)))";
        
        ConstraintSyntaxTree tree = ConditionUtils.parseCString(formula);
        
        OCLFeatureCall firstOr = checkOclFeatureCall(tree, OclKeyWords.OR);
        checkVariable(firstOr.getOperand(), "A");
        OCLFeatureCall leftNot = checkOclFeatureCall(firstOr.getParameter(0), OclKeyWords.NOT);
        
        OCLFeatureCall outerAnd = checkOclFeatureCall(leftNot.getOperand(), OclKeyWords.AND);
        checkVariable(outerAnd.getOperand(), "B");
        
        OCLFeatureCall secondOr = checkOclFeatureCall(outerAnd.getParameter(0), OclKeyWords.OR);
        
        OCLFeatureCall leftOr = checkOclFeatureCall(secondOr.getOperand(), OclKeyWords.OR);
        checkVariable(leftOr.getOperand(), "C");
        OCLFeatureCall rightNot = checkOclFeatureCall(leftOr.getParameter(0), OclKeyWords.NOT);
        checkVariable(rightNot.getOperand(), "D");
        
        OCLFeatureCall innerAnd = checkOclFeatureCall(secondOr.getParameter(0), OclKeyWords.AND);
        checkVariable(innerAnd.getOperand(), "E");
        checkVariable(innerAnd.getParameter(0), "A");
    }
    
    private static void checkVariable(ConstraintSyntaxTree tree, String expectedName) {
        Assert.assertTrue(tree instanceof Variable);
        Variable var = (Variable) tree;
        Assert.assertEquals(expectedName, var.getVariable().getName());
    }
    
    private static OCLFeatureCall checkOclFeatureCall(ConstraintSyntaxTree tree, String expectedOperation) {
        Assert.assertTrue(tree instanceof OCLFeatureCall);
        OCLFeatureCall call = (OCLFeatureCall) tree;
        Assert.assertEquals(expectedOperation, call.getOperation());
        return call;
    }
    
    @Test
    public void testToCStringSimpleVariable() throws ConstraintException {
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        
        ConstraintSyntaxTree tree = a;
        
        String result = ConditionUtils.toCString(tree);
        
        Assert.assertEquals("A", result);
    }
    
    @Test
    public void testToCStringSimpleOr() throws ConstraintException {
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        Variable b = new Variable(new DecisionVariableDeclaration("B", BooleanType.TYPE, null));
        
        ConstraintSyntaxTree tree = new OCLFeatureCall(a, OclKeyWords.OR, b);
        
        String result = ConditionUtils.toCString(tree);
        
        Assert.assertEquals("A || B", result);
    }
    
    @Test
    public void testToCStringSimpleAnd() throws ConstraintException {
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        Variable b = new Variable(new DecisionVariableDeclaration("B", BooleanType.TYPE, null));
        
        ConstraintSyntaxTree tree = new OCLFeatureCall(a, OclKeyWords.AND, b);
        
        String result = ConditionUtils.toCString(tree);
        
        Assert.assertEquals("A && B", result);
    }
    
    @Test
    public void testToCStringNotInOr() throws ConstraintException {
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        Variable b = new Variable(new DecisionVariableDeclaration("B", BooleanType.TYPE, null));
        
        ConstraintSyntaxTree tree = new OCLFeatureCall(a, OclKeyWords.OR,
                new OCLFeatureCall(b, OclKeyWords.NOT));
        
        String result = ConditionUtils.toCString(tree);
        
        Assert.assertEquals("A || !B", result);
    }
    
    @Test
    public void testToCStringNotInAnd() throws ConstraintException {
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        Variable b = new Variable(new DecisionVariableDeclaration("B", BooleanType.TYPE, null));
        
        ConstraintSyntaxTree tree = new OCLFeatureCall(new OCLFeatureCall(a, OclKeyWords.NOT),
                OclKeyWords.AND, b);
        
        String result = ConditionUtils.toCString(tree);
        
        Assert.assertEquals("!A && B", result);
    }
    
    @Test
    public void testToCStringParenthesis() throws ConstraintException {
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        Variable b = new Variable(new DecisionVariableDeclaration("B", BooleanType.TYPE, null));
        Variable c = new Variable(new DecisionVariableDeclaration("C", BooleanType.TYPE, null));
        
        ConstraintSyntaxTree tree = new OCLFeatureCall(new OCLFeatureCall(a, OclKeyWords.OR, c),
                OclKeyWords.AND, b);
        
        String result = ConditionUtils.toCString(tree);
        
        Assert.assertEquals("(A || C) && B", result);
    }
    
    @Test
    public void testToCStringComplex() throws ConstraintException {
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        Variable b = new Variable(new DecisionVariableDeclaration("B", BooleanType.TYPE, null));
        Variable c = new Variable(new DecisionVariableDeclaration("C", BooleanType.TYPE, null));
        Variable d = new Variable(new DecisionVariableDeclaration("D", BooleanType.TYPE, null));
        Variable e = new Variable(new DecisionVariableDeclaration("E", BooleanType.TYPE, null));
        
        ConstraintSyntaxTree tree =
            new OCLFeatureCall(
                a,
            OclKeyWords.OR, 
                new OCLFeatureCall(
                    new OCLFeatureCall(
                        b,
                    OclKeyWords.AND,
                        new OCLFeatureCall(
                            new OCLFeatureCall(
                                c, 
                            OclKeyWords.OR,
                                new OCLFeatureCall(
                                    d,
                                OclKeyWords.NOT)
                            ),
                        OclKeyWords.OR,
                            new OCLFeatureCall(
                                e,
                            OclKeyWords.AND,
                                a)
                        )
                    ),
                OclKeyWords.NOT
                )
            );
        
        String result = ConditionUtils.toCString(tree);
        
        Assert.assertEquals("A || !(B && ((C || !D) || (E && A)))", result);
    }
    
}
