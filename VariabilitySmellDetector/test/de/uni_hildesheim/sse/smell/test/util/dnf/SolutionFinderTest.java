package de.uni_hildesheim.sse.smell.test.util.dnf;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.smell.util.ConstraintException;
import de.uni_hildesheim.sse.smell.util.dnf.Solution;
import de.uni_hildesheim.sse.smell.util.dnf.SolutionFinder;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.values.BooleanValue;

public class SolutionFinderTest {

    @Test
    public void testSimpleVariable() throws ConstraintException {
        SolutionFinder finder = new SolutionFinder();
        
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        
        ConstraintSyntaxTree formula = a;
        
        Set<Solution> solutions = finder.getSolutions(formula);
        
        Assert.assertEquals(1, solutions.size());
        
        Solution toTest1 = new Solution();
        toTest1.setValue("A", true);
        testConstainsSolution(solutions, toTest1);
    }
    
    @Test
    public void testSimpleNegation() throws ConstraintException {
        SolutionFinder finder = new SolutionFinder();
        
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        
        ConstraintSyntaxTree formula = new OCLFeatureCall(a, OclKeyWords.NOT);
        
        Set<Solution> solutions = finder.getSolutions(formula);
        
        Assert.assertEquals(1, solutions.size());
        
        Solution toTest1 = new Solution();
        toTest1.setValue("A", false);
        testConstainsSolution(solutions, toTest1);
    }
    
    @Test
    public void testSimpleOr() throws ConstraintException {
        SolutionFinder finder = new SolutionFinder();
        
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        Variable b = new Variable(new DecisionVariableDeclaration("B", BooleanType.TYPE, null));
        
        ConstraintSyntaxTree formula = new OCLFeatureCall(a, OclKeyWords.OR, b);
        
        Set<Solution> solutions = finder.getSolutions(formula);
        
        Assert.assertEquals(3, solutions.size());
        
        Solution toTest1 = new Solution();
        toTest1.setValue("A", true);
        toTest1.setValue("B", false);
        testConstainsSolution(solutions, toTest1);
        
        Solution toTest2 = new Solution();
        toTest2.setValue("A", false);
        toTest2.setValue("B", true);
        testConstainsSolution(solutions, toTest2);
        
        Solution toTest3 = new Solution();
        toTest3.setValue("A", true);
        toTest3.setValue("B", true);
        testConstainsSolution(solutions, toTest3);
    }
    
    @Test
    public void testSimpleAnd() throws ConstraintException {
        SolutionFinder finder = new SolutionFinder();
        
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        Variable b = new Variable(new DecisionVariableDeclaration("B", BooleanType.TYPE, null));
        
        ConstraintSyntaxTree formula = new OCLFeatureCall(a, OclKeyWords.AND, b);
        
        Set<Solution> solutions = finder.getSolutions(formula);
        
        Assert.assertEquals(1, solutions.size());
        
        Solution toTest1 = new Solution();
        toTest1.setValue("A", true);
        toTest1.setValue("B", true);
        testConstainsSolution(solutions, toTest1);
    }
    
    @Test
    public void testTrueConstant() throws ConstraintException {
        SolutionFinder finder = new SolutionFinder();
        
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        ConstantValue treu = new ConstantValue(BooleanValue.TRUE);
        
        ConstraintSyntaxTree formula = new OCLFeatureCall(treu, OclKeyWords.AND, a);
        
        Set<Solution> solutions = finder.getSolutions(formula);
        
        Assert.assertEquals(1, solutions.size());
        
        Solution toTest1 = new Solution();
        toTest1.setValue("A", true);
        testConstainsSolution(solutions, toTest1);
    }
    
    @Test
    public void testFalseConstant() throws ConstraintException {
        SolutionFinder finder = new SolutionFinder();
        
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        ConstantValue fasle = new ConstantValue(BooleanValue.FALSE);
        
        ConstraintSyntaxTree formula = new OCLFeatureCall(fasle, OclKeyWords.AND, a);
        
        Set<Solution> solutions = finder.getSolutions(formula);
        
        Assert.assertEquals(0, solutions.size());
    }
    
    @Test
    public void testDoubleVariable() throws ConstraintException {
        SolutionFinder finder = new SolutionFinder();
        
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        Variable b = new Variable(new DecisionVariableDeclaration("B", BooleanType.TYPE, null));
        Variable c = new Variable(new DecisionVariableDeclaration("C", BooleanType.TYPE, null));

        ConstraintSyntaxTree aAndB = new OCLFeatureCall(a, OclKeyWords.AND, b);
        ConstraintSyntaxTree aAndNotC = new OCLFeatureCall(a, OclKeyWords.AND, new OCLFeatureCall(c, OclKeyWords.NOT));
        
        ConstraintSyntaxTree formula = new OCLFeatureCall(aAndB, OclKeyWords.OR, aAndNotC);
        
        Set<Solution> solutions = finder.getSolutions(formula);
        
        Assert.assertEquals(3, solutions.size());
        
        Solution toTest1 = new Solution();
        toTest1.setValue("A", true);
        toTest1.setValue("B", false);
        toTest1.setValue("C", false);
        testConstainsSolution(solutions, toTest1);
        
        Solution toTest2 = new Solution();
        toTest2.setValue("A", true);
        toTest2.setValue("B", true);
        toTest2.setValue("C", false);
        testConstainsSolution(solutions, toTest2);
        
        Solution toTest3 = new Solution();
        toTest3.setValue("A", true);
        toTest3.setValue("B", true);
        toTest3.setValue("C", true);
        testConstainsSolution(solutions, toTest3);
    }
    
    @Test
    public void testComplex() throws ConstraintException {
        SolutionFinder finder = new SolutionFinder();
        
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        Variable b = new Variable(new DecisionVariableDeclaration("B", BooleanType.TYPE, null));
        Variable c = new Variable(new DecisionVariableDeclaration("C", BooleanType.TYPE, null));
        
        ConstraintSyntaxTree formula = new OCLFeatureCall(a, OclKeyWords.AND,
                new OCLFeatureCall(new OCLFeatureCall(b, OclKeyWords.NOT), OclKeyWords.OR, c));
        
        Set<Solution> solutions = finder.getSolutions(formula);
        
        Assert.assertEquals(3, solutions.size());
        
        Solution toTest1 = new Solution();
        toTest1.setValue("A", true);
        toTest1.setValue("B", false);
        toTest1.setValue("C", false);
        testConstainsSolution(solutions, toTest1);
        
        Solution toTest2 = new Solution();
        toTest2.setValue("A", true);
        toTest2.setValue("B", false);
        toTest2.setValue("C", true);
        testConstainsSolution(solutions, toTest2);
        
        Solution toTest3 = new Solution();
        toTest3.setValue("A", true);
        toTest3.setValue("B", true);
        toTest3.setValue("C", true);
        testConstainsSolution(solutions, toTest3);
    }
    
    private void testConstainsSolution(Set<Solution> solutions, Solution toTest) {
        for (Solution s : solutions) {
            if (s.equals(toTest)) {
                return;
            }
        }
        
        Assert.fail("Solution not found: " + toTest);
    }
    
    @Test
    public void testRuntime() throws ConstraintException {
        // TODO: increase this for actual runtime measurements
        ConstraintSyntaxTree formula = createTreeWithSize(10, OclKeyWords.OR);
        
        SolutionFinder finder = new SolutionFinder();
        finder.getSolutions(formula);
    }
 
    private ConstraintSyntaxTree createTreeWithSize(int size, String operation) throws ConstraintException {
        Variable var = new Variable(new DecisionVariableDeclaration("VAR_" + size, BooleanType.TYPE, null));
        if (size > 1) {
            return new OCLFeatureCall(var, operation, createTreeWithSize(size - 1, operation));
        } else {
            return var;
        }
    }
    
}
