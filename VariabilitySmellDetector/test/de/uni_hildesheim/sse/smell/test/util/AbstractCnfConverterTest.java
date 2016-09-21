package de.uni_hildesheim.sse.smell.test.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import de.uni_hildesheim.sse.smell.util.ICnfConverter;
import de.uni_hildesheim.sse.smell.util.VariableToNumberConverter;
import net.ssehub.easy.varModel.cst.AttributeVariable;
import net.ssehub.easy.varModel.cst.BlockExpression;
import net.ssehub.easy.varModel.cst.Comment;
import net.ssehub.easy.varModel.cst.CompoundAccess;
import net.ssehub.easy.varModel.cst.CompoundInitializer;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.ContainerInitializer;
import net.ssehub.easy.varModel.cst.ContainerOperationCall;
import net.ssehub.easy.varModel.cst.IConstraintTreeVisitor;
import net.ssehub.easy.varModel.cst.IfThen;
import net.ssehub.easy.varModel.cst.Let;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Parenthesis;
import net.ssehub.easy.varModel.cst.Self;
import net.ssehub.easy.varModel.cst.UnresolvedExpression;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.filter.DeclrationInConstraintFinder;
import net.ssehub.easy.varModel.model.values.BooleanValue;

public abstract class AbstractCnfConverterTest {
    
    private ICnfConverter converter;
    
    protected AbstractCnfConverterTest(ICnfConverter converter) {
        this.converter = converter;
    }
   
    @Test
    public void testSimpleVariable() throws Exception {
        DecisionVariableDeclaration aDecl = new DecisionVariableDeclaration("A", BooleanType.TYPE, null);
        
        AbstractVariable[] vars = { aDecl };
        
        Variable a = new Variable(aDecl);
        
        ConstraintSyntaxTree formula1 = a;
        ConstraintSyntaxTree formula2 = new OCLFeatureCall(a, OclKeyWords.NOT);
        
        runTest(formula1, vars, "testdata/cnfConverter/testSimpleVariable.1.dimacs");
        runTest(formula2, vars, "testdata/cnfConverter/testSimpleVariable.2.dimacs");
    }
    
    @Test
    public void testSimpleOr() throws Exception {
        DecisionVariableDeclaration aDecl = new DecisionVariableDeclaration("A", BooleanType.TYPE, null);
        DecisionVariableDeclaration bDecl = new DecisionVariableDeclaration("B", BooleanType.TYPE, null);
        
        AbstractVariable[] vars = { aDecl, bDecl };
        
        Variable a = new Variable(aDecl);
        Variable b = new Variable(bDecl);
        OCLFeatureCall notA = new OCLFeatureCall(a, OclKeyWords.NOT);
        OCLFeatureCall notB = new OCLFeatureCall(b, OclKeyWords.NOT);
        
        ConstraintSyntaxTree formula1 = new OCLFeatureCall(a, OclKeyWords.OR, b);
        ConstraintSyntaxTree formula2 = new OCLFeatureCall(notA, OclKeyWords.OR, b);
        ConstraintSyntaxTree formula3 = new OCLFeatureCall(a, OclKeyWords.OR, notB);
        ConstraintSyntaxTree formula4 = new OCLFeatureCall(notA, OclKeyWords.OR, notB);
        
        runTest(formula1, vars, "testdata/cnfConverter/testSimpleOr.1.dimacs");
        runTest(formula2, vars, "testdata/cnfConverter/testSimpleOr.2.dimacs");
        runTest(formula3, vars, "testdata/cnfConverter/testSimpleOr.3.dimacs");
        runTest(formula4, vars, "testdata/cnfConverter/testSimpleOr.4.dimacs");
    }
    
    @Test
    public void testSimpleAnd() throws Exception {
        DecisionVariableDeclaration aDecl = new DecisionVariableDeclaration("A", BooleanType.TYPE, null);
        DecisionVariableDeclaration bDecl = new DecisionVariableDeclaration("B", BooleanType.TYPE, null);
        
        AbstractVariable[] vars = { aDecl, bDecl };
        
        Variable a = new Variable(aDecl);
        Variable b = new Variable(bDecl);
        OCLFeatureCall notA = new OCLFeatureCall(a, OclKeyWords.NOT);
        OCLFeatureCall notB = new OCLFeatureCall(b, OclKeyWords.NOT);
        
        ConstraintSyntaxTree formula1 = new OCLFeatureCall(a, OclKeyWords.AND, b);
        ConstraintSyntaxTree formula2 = new OCLFeatureCall(notA, OclKeyWords.AND, b);
        ConstraintSyntaxTree formula3 = new OCLFeatureCall(a, OclKeyWords.AND, notB);
        ConstraintSyntaxTree formula4 = new OCLFeatureCall(notA, OclKeyWords.AND, notB);
        
        runTest(formula1, vars, "testdata/cnfConverter/testSimpleAnd.1.dimacs");
        runTest(formula2, vars, "testdata/cnfConverter/testSimpleAnd.2.dimacs");
        runTest(formula3, vars, "testdata/cnfConverter/testSimpleAnd.3.dimacs");
        runTest(formula4, vars, "testdata/cnfConverter/testSimpleAnd.4.dimacs");
    }
    
    @Test
    public void testAndNestedInOr() throws Exception {
        DecisionVariableDeclaration aDecl = new DecisionVariableDeclaration("A", BooleanType.TYPE, null);
        DecisionVariableDeclaration bDecl = new DecisionVariableDeclaration("B", BooleanType.TYPE, null);
        DecisionVariableDeclaration cDecl = new DecisionVariableDeclaration("C", BooleanType.TYPE, null);
        
        AbstractVariable[] vars = { aDecl, bDecl, cDecl };
        
        Variable a = new Variable(aDecl);
        Variable b = new Variable(bDecl);
        Variable c = new Variable(cDecl);
        OCLFeatureCall notA = new OCLFeatureCall(a, OclKeyWords.NOT);
        OCLFeatureCall notB = new OCLFeatureCall(b, OclKeyWords.NOT);
        OCLFeatureCall notC = new OCLFeatureCall(c, OclKeyWords.NOT);
        
        OCLFeatureCall formula1 = new OCLFeatureCall(a, OclKeyWords.OR, new OCLFeatureCall(b, OclKeyWords.AND, c));
        OCLFeatureCall formula2 = new OCLFeatureCall(notA, OclKeyWords.OR, new OCLFeatureCall(notB, OclKeyWords.AND, notC));
        OCLFeatureCall formula3 = new OCLFeatureCall(a, OclKeyWords.OR, new OCLFeatureCall(notB, OclKeyWords.AND, c));
        OCLFeatureCall formula4 = new OCLFeatureCall(notA, OclKeyWords.OR, new OCLFeatureCall(b, OclKeyWords.AND, notC));
        
        runTest(formula1, vars, "testdata/cnfConverter/testAndNestedInOr.1.dimacs");
        runTest(formula2, vars, "testdata/cnfConverter/testAndNestedInOr.2.dimacs");
        runTest(formula3, vars, "testdata/cnfConverter/testAndNestedInOr.3.dimacs");
        runTest(formula4, vars, "testdata/cnfConverter/testAndNestedInOr.4.dimacs");
    }
    
    @Test
    public void testOrNestedInAnd() throws Exception {
        DecisionVariableDeclaration aDecl = new DecisionVariableDeclaration("A", BooleanType.TYPE, null);
        DecisionVariableDeclaration bDecl = new DecisionVariableDeclaration("B", BooleanType.TYPE, null);
        DecisionVariableDeclaration cDecl = new DecisionVariableDeclaration("C", BooleanType.TYPE, null);
        
        AbstractVariable[] vars = { aDecl, bDecl, cDecl };
        
        Variable a = new Variable(aDecl);
        Variable b = new Variable(bDecl);
        Variable c = new Variable(cDecl);
        OCLFeatureCall notA = new OCLFeatureCall(a, OclKeyWords.NOT);
        OCLFeatureCall notB = new OCLFeatureCall(b, OclKeyWords.NOT);
        OCLFeatureCall notC = new OCLFeatureCall(c, OclKeyWords.NOT);
        
        OCLFeatureCall formula1 = new OCLFeatureCall(a, OclKeyWords.AND, new OCLFeatureCall(b, OclKeyWords.OR, c));
        OCLFeatureCall formula2 = new OCLFeatureCall(notA, OclKeyWords.AND, new OCLFeatureCall(notB, OclKeyWords.OR, notC));
        OCLFeatureCall formula3 = new OCLFeatureCall(a, OclKeyWords.AND, new OCLFeatureCall(notB, OclKeyWords.OR, c));
        OCLFeatureCall formula4 = new OCLFeatureCall(notA, OclKeyWords.AND, new OCLFeatureCall(b, OclKeyWords.OR, notC));
        
        runTest(formula1, vars, "testdata/cnfConverter/testOrNestedInAnd.1.dimacs");
        runTest(formula2, vars, "testdata/cnfConverter/testOrNestedInAnd.2.dimacs");
        runTest(formula3, vars, "testdata/cnfConverter/testOrNestedInAnd.3.dimacs");
        runTest(formula4, vars, "testdata/cnfConverter/testOrNestedInAnd.4.dimacs");
    }
    
    @Test
    public void testHighLevelNot() throws Exception {
        DecisionVariableDeclaration aDecl = new DecisionVariableDeclaration("A", BooleanType.TYPE, null);
        DecisionVariableDeclaration bDecl = new DecisionVariableDeclaration("B", BooleanType.TYPE, null);
        DecisionVariableDeclaration cDecl = new DecisionVariableDeclaration("C", BooleanType.TYPE, null);
        
        AbstractVariable[] vars = { aDecl, bDecl, cDecl };
        
        Variable a = new Variable(aDecl);
        Variable b = new Variable(bDecl);
        Variable c = new Variable(cDecl);
        
        OCLFeatureCall formula1 = new OCLFeatureCall(a, OclKeyWords.AND,
                new OCLFeatureCall(new OCLFeatureCall(b, OclKeyWords.OR, c), OclKeyWords.NOT));
        OCLFeatureCall formula2 = new OCLFeatureCall( 
                new OCLFeatureCall(a, OclKeyWords.AND, new OCLFeatureCall(b, OclKeyWords.OR, c)),
                OclKeyWords.NOT);
        OCLFeatureCall formula3 = new OCLFeatureCall(a, OclKeyWords.OR,
                new OCLFeatureCall(new OCLFeatureCall(b, OclKeyWords.AND, c), OclKeyWords.NOT));
        OCLFeatureCall formula4 = new OCLFeatureCall(  
                new OCLFeatureCall(a, OclKeyWords.OR, new OCLFeatureCall(b, OclKeyWords.AND, c)),
                OclKeyWords.NOT);
        
        runTest(formula1, vars, "testdata/cnfConverter/testHighLevelNot.1.dimacs");
        runTest(formula2, vars, "testdata/cnfConverter/testHighLevelNot.2.dimacs");
        runTest(formula3, vars, "testdata/cnfConverter/testHighLevelNot.3.dimacs");
        runTest(formula4, vars, "testdata/cnfConverter/testHighLevelNot.4.dimacs");
    }
    
    @Test
    public void testDoubleNot() throws Exception {
        DecisionVariableDeclaration aDecl = new DecisionVariableDeclaration("A", BooleanType.TYPE, null);
        DecisionVariableDeclaration bDecl = new DecisionVariableDeclaration("B", BooleanType.TYPE, null);
        
        AbstractVariable[] vars = { aDecl, bDecl };
        
        Variable a = new Variable(aDecl);
        Variable b = new Variable(bDecl);
        OCLFeatureCall notA = new OCLFeatureCall(a, OclKeyWords.NOT);
        OCLFeatureCall notB = new OCLFeatureCall(b, OclKeyWords.NOT);
        
        OCLFeatureCall formula1 = new OCLFeatureCall(notA, OclKeyWords.NOT);
        OCLFeatureCall formula2 = new OCLFeatureCall(new OCLFeatureCall(
                new OCLFeatureCall(a, OclKeyWords.OR, notB), OclKeyWords.NOT), OclKeyWords.NOT);
        OCLFeatureCall formula3 = new OCLFeatureCall(new OCLFeatureCall(
                new OCLFeatureCall(a, OclKeyWords.AND, b), OclKeyWords.NOT), OclKeyWords.NOT);
        
        runTest(formula1, new AbstractVariable[] { aDecl }, "testdata/cnfConverter/testDoubleNot.1.dimacs");
        runTest(formula2, vars, "testdata/cnfConverter/testDoubleNot.2.dimacs");
        runTest(formula3, vars, "testdata/cnfConverter/testDoubleNot.3.dimacs");
    }
    
    @Test
    public void testComplexOr() throws Exception {
        DecisionVariableDeclaration aDecl = new DecisionVariableDeclaration("A", BooleanType.TYPE, null);
        DecisionVariableDeclaration bDecl = new DecisionVariableDeclaration("B", BooleanType.TYPE, null);
        DecisionVariableDeclaration cDecl = new DecisionVariableDeclaration("C", BooleanType.TYPE, null);
        
        AbstractVariable[] vars = { aDecl, bDecl, cDecl };
        
        Variable a = new Variable(aDecl);
        Variable b = new Variable(bDecl);
        Variable c = new Variable(cDecl);
        OCLFeatureCall notA = new OCLFeatureCall(a, OclKeyWords.NOT);
        OCLFeatureCall notB = new OCLFeatureCall(b, OclKeyWords.NOT);
        OCLFeatureCall notC = new OCLFeatureCall(c, OclKeyWords.NOT);
        
        OCLFeatureCall formula1 = new OCLFeatureCall(
                new OCLFeatureCall(a, OclKeyWords.OR, notB), OclKeyWords.OR, new OCLFeatureCall(b, OclKeyWords.AND, c));
        OCLFeatureCall formula2 = new OCLFeatureCall(
                new OCLFeatureCall(notA, OclKeyWords.OR, notB), OclKeyWords.OR, new OCLFeatureCall(notB, OclKeyWords.AND, notC));
        OCLFeatureCall formula3 = new OCLFeatureCall(
                new OCLFeatureCall(a, OclKeyWords.AND, notB), OclKeyWords.OR, new OCLFeatureCall(notB, OclKeyWords.AND, c));
        OCLFeatureCall formula4 = new OCLFeatureCall(
                new OCLFeatureCall(notA, OclKeyWords.AND, notB), OclKeyWords.OR, new OCLFeatureCall(b, OclKeyWords.AND, notC));
        
        runTest(formula1, vars, "testdata/cnfConverter/testComplexOr.1.dimacs");
        runTest(formula2, vars, "testdata/cnfConverter/testComplexOr.2.dimacs");
        runTest(formula3, vars, "testdata/cnfConverter/testComplexOr.3.dimacs");
        runTest(formula4, vars, "testdata/cnfConverter/testComplexOr.4.dimacs");
    }
    
    @Test
    public void testConstants() throws Exception {
        DecisionVariableDeclaration aDecl = new DecisionVariableDeclaration("A", BooleanType.TYPE, null);
        DecisionVariableDeclaration bDecl = new DecisionVariableDeclaration("B", BooleanType.TYPE, null);
        
        AbstractVariable[] vars = { aDecl, bDecl };
        
        Variable a = new Variable(aDecl);
        Variable b = new Variable(bDecl);
        OCLFeatureCall notA = new OCLFeatureCall(a, OclKeyWords.NOT);
        OCLFeatureCall notB = new OCLFeatureCall(b, OclKeyWords.NOT);
        
        ConstantValue treu = new ConstantValue(BooleanValue.TRUE);
        ConstantValue fasle = new ConstantValue(BooleanValue.FALSE);
        
        OCLFeatureCall formula1 = new OCLFeatureCall(a, OclKeyWords.AND, new OCLFeatureCall(notB, OclKeyWords.OR, fasle));
        OCLFeatureCall formula2 = new OCLFeatureCall(notA, OclKeyWords.AND, new OCLFeatureCall(b, OclKeyWords.OR, fasle));
        OCLFeatureCall formula3 = new OCLFeatureCall(a, OclKeyWords.OR, new OCLFeatureCall(notB, OclKeyWords.AND, treu));
        OCLFeatureCall formula4 = new OCLFeatureCall(notA, OclKeyWords.OR, new OCLFeatureCall(b, OclKeyWords.AND, treu));
        
        runTest(formula1, vars, "testdata/cnfConverter/testConstants.1.dimacs");
        runTest(formula2, vars, "testdata/cnfConverter/testConstants.2.dimacs");
        runTest(formula3, vars, "testdata/cnfConverter/testConstants.3.dimacs");
        runTest(formula4, vars, "testdata/cnfConverter/testConstants.4.dimacs");
    }
    
    private void runTest(ConstraintSyntaxTree formula, AbstractVariable[] vars, String expectedDimacsFile) throws Exception {
        List<ConstraintSyntaxTree> terms = converter.convertToCnf(formula);
        
        VariableToNumberConverter termsVarConverter = new VariableToNumberConverter();
        VariableToNumberConverter expectedVarConverter = new VariableToNumberConverter(expectedDimacsFile, "");
        
        CnfChecker checker = new CnfChecker();
        for (ConstraintSyntaxTree term : terms) {
            term.accept(checker);
            DeclrationInConstraintFinder finder = new DeclrationInConstraintFinder(term);
            for (AbstractVariable var : finder.getDeclarations()) {
                termsVarConverter.addVarible(var.getName());
            }
            
        }
        
        // also add "missing" variables to termsVarConverter, since a CnfConverter
        // may optimize them away
        for (AbstractVariable var : vars) {
            termsVarConverter.addVarible(var.getName());
        }
        
        boolean[] values = new boolean[vars.length];
        for (int i = 0; i < (int) (Math.pow(2, vars.length)); i++) {
            for (int j = 0; j < values.length; j++) {
                values[j] = (i & (1 << j)) != 0;
            }
            
            /* check if both, terms and expectedDimacs are equisatifiable */
            ISolver termsSolver = getSolver(null);
            for (ConstraintSyntaxTree cst : terms) {
                int[] numbers = termsVarConverter.convertToDimacs(cst);
                termsSolver.addClause(new VecInt(numbers));
            }
            
            
            ISolver expectedSolver = getSolver(expectedDimacsFile);
            
            int[] termsValues = new int[values.length];
            int[] expectedValues = new int[values.length];
            for (int j = 0; j < values.length; j++) {
                termsValues[j] = termsVarConverter.getNumber(vars[j].getName());
                expectedValues[j] = expectedVarConverter.getNumber(vars[j].getName());
                
                if (!values[j]) {
                    termsValues[j] *= -1;
                    expectedValues[j] *= -1;
                }
            }
            
            Assert.assertEquals("Not equisatisfiable for " + Arrays.toString(expectedValues),
                    isSolvable(expectedSolver, expectedValues), isSolvable(termsSolver, termsValues));
        }
        
    }
    
    private static boolean isSolvable(ISolver solver, int[] values) throws TimeoutException {
        for (int i = 0; i < values.length; i++) {
            try {
                solver.addClause(new VecInt(new int[] { values[i] }));
            } catch (ContradictionException e) {
                return false;
            }
        }
        return solver.isSatisfiable();
    }
    
    private static ISolver getSolver(String path) {
        ISolver solver = SolverFactory.newDefault();
        solver.setDBSimplificationAllowed(false);
        if (path != null) {
            Reader reader = new DimacsReader(solver);
            boolean successfulParsed = false;
            try {
                reader.parseInstance(path);
                successfulParsed = true;
            } catch (ParseFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            if (!successfulParsed) {
                solver = null;
            }
        }
        return solver;
    }
    
    private static class CnfChecker implements IConstraintTreeVisitor {

        @Override
        public void visitConstantValue(ConstantValue value) {
            Assert.fail();
        }

        @Override
        public void visitVariable(Variable variable) {
        }

        @Override
        public void visitAnnotationVariable(AttributeVariable variable) {
            Assert.fail();
        }

        @Override
        public void visitParenthesis(Parenthesis parenthesis) {
            Assert.fail();
        }

        @Override
        public void visitComment(Comment comment) {
            Assert.fail();
        }

        @Override
        public void visitOclFeatureCall(OCLFeatureCall call) {
            if (call.getOperation().equals(OclKeyWords.NOT)) {
                Assert.assertTrue(call.getOperand() instanceof Variable);
            } else if (call.getOperation().equals(OclKeyWords.OR)) {
                call.getOperand().accept(this);
                call.getParameter(0).accept(this);
            } else {
                Assert.fail();
            }
            
        }

        @Override
        public void visitLet(Let let) {
            Assert.fail();
        }

        @Override
        public void visitIfThen(IfThen ifThen) {
            Assert.fail();
        }

        @Override
        public void visitContainerOperationCall(ContainerOperationCall call) {
            Assert.fail();
        }

        @Override
        public void visitCompoundAccess(CompoundAccess access) {
            Assert.fail();
        }

        @Override
        public void visitUnresolvedExpression(UnresolvedExpression expression) {
            Assert.fail();
        }

        @Override
        public void visitCompoundInitializer(CompoundInitializer initializer) {
            Assert.fail();
        }

        @Override
        public void visitContainerInitializer(ContainerInitializer initializer) {
            Assert.fail();
        }

        @Override
        public void visitSelf(Self self) {
            Assert.fail();
        }

        @Override
        public void visitBlockExpression(BlockExpression block) {
            Assert.fail();
        }
        
    }
    
}
