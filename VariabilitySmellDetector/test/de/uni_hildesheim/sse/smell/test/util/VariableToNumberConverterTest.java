package de.uni_hildesheim.sse.smell.test.util;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.smell.util.VarNotFoundException;
import de.uni_hildesheim.sse.smell.util.VariableToNumberConverter;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;

public class VariableToNumberConverterTest {

    @Test
    public void testRead() throws Exception {
        String file = "testdata/variableToNumberConverter/testModel.dimacs";
        VariableToNumberConverter conv = new VariableToNumberConverter(file, "");
        
        Assert.assertEquals(1, conv.getNumber("A"));
        Assert.assertEquals(2, conv.getNumber("vaRiaBle"));
        Assert.assertEquals(3, conv.getNumber("VA_rIABLE"));
        Assert.assertEquals(4, conv.getNumber("test"));
        
        try {
            conv.getNumber("not_existing");
            Assert.fail();
        } catch (VarNotFoundException e) {
            Assert.assertEquals("not_existing", e.getName());
        }
        
        Assert.assertEquals("A", conv.getName(1));
        Assert.assertEquals("vaRiaBle", conv.getName(2));
        Assert.assertEquals("VA_rIABLE", conv.getName(3));
        Assert.assertEquals("test", conv.getName(4));
        Assert.assertEquals(null, conv.getName(5));
    }
    
    @Test
    public void testReadWithPrefix() throws Exception {
        String file = "testdata/variableToNumberConverter/testModel.dimacs";
        VariableToNumberConverter conv = new VariableToNumberConverter(file, "PREFIX_");
        
        Assert.assertEquals(1, conv.getNumber("PREFIX_A"));
        Assert.assertEquals(2, conv.getNumber("PREFIX_vaRiaBle"));
        Assert.assertEquals(3, conv.getNumber("PREFIX_VA_rIABLE"));
        Assert.assertEquals(4, conv.getNumber("PREFIX_test"));
        
        try {
            conv.getNumber("A");
            Assert.fail();
        } catch (VarNotFoundException e) {
            Assert.assertEquals("A", e.getName());
        }
        
        try {
            conv.getNumber("vaRiaBle");
            Assert.fail();
        } catch (VarNotFoundException e) {
            Assert.assertEquals("vaRiaBle", e.getName());
        }
        
        try {
            conv.getNumber("VA_rIABLE");
            Assert.fail();
        } catch (VarNotFoundException e) {
            Assert.assertEquals("VA_rIABLE", e.getName());
        }
        
        try {
            conv.getNumber("test");
            Assert.fail();
        } catch (VarNotFoundException e) {
            Assert.assertEquals("test", e.getName());
        }
        
        Assert.assertEquals("PREFIX_A", conv.getName(1));
        Assert.assertEquals("PREFIX_vaRiaBle", conv.getName(2));
        Assert.assertEquals("PREFIX_VA_rIABLE", conv.getName(3));
        Assert.assertEquals("PREFIX_test", conv.getName(4));
        Assert.assertEquals(null, conv.getName(5));
    }
    
    @Test
    public void testAddVariable() throws Exception {
        VariableToNumberConverter conv = new VariableToNumberConverter();
        
        Assert.assertEquals(null, conv.getName(1));
        Assert.assertEquals(1, conv.addVarible("var_1"));
        Assert.assertEquals(1, conv.getNumber("var_1"));
        Assert.assertEquals("var_1", conv.getName(1));
        
        Assert.assertEquals(null, conv.getName(2));
        Assert.assertEquals(2, conv.addVarible("var_2"));
        Assert.assertEquals(2, conv.getNumber("var_2"));
        Assert.assertEquals("var_2", conv.getName(2));
        
        Assert.assertEquals(null, conv.getName(3));
        Assert.assertEquals(3, conv.addVarible("var_3"));
        Assert.assertEquals(3, conv.getNumber("var_3"));
        Assert.assertEquals("var_3", conv.getName(3));
        
        Assert.assertEquals(2, conv.addVarible("var_2"));
        Assert.assertEquals(2, conv.getNumber("var_2"));
        Assert.assertEquals("var_2", conv.getName(2));
    }
    
    @Test
    public void testConvertToDimacs() throws Exception {
        Variable a = new Variable(new DecisionVariableDeclaration("A", BooleanType.TYPE, null));
        Variable b = new Variable(new DecisionVariableDeclaration("B", BooleanType.TYPE, null));
        Variable c = new Variable(new DecisionVariableDeclaration("C", BooleanType.TYPE, null));
        Variable d = new Variable(new DecisionVariableDeclaration("D", BooleanType.TYPE, null));
        Variable[] vars = {a, b, c, d};
        
        VariableToNumberConverter conv = new VariableToNumberConverter();
        
        for (int i = 0; i < vars.length; i++) {
            Assert.assertEquals(i + 1, conv.addVarible(vars[i].getVariable().getName()));
        }
        
        int[] a1 = { 1, 2, 3, 4};
        Assert.assertArrayEquals(a1, conv.convertToDimacs(buildCnf(vars, a1)));
        
        int[] a2 = { -1, 2, -3, 4};
        Assert.assertArrayEquals(a2, conv.convertToDimacs(buildCnf(vars, a2)));
        
        int[] a3 = { 1, -2, 3, -4};
        Assert.assertArrayEquals(a3, conv.convertToDimacs(buildCnf(vars, a3)));
        
        int[] a4 = { -1, -2, 3, -4};
        Assert.assertArrayEquals(a4, conv.convertToDimacs(buildCnf(vars, a4)));
    }
    
    private ConstraintSyntaxTree buildCnf(Variable[] vars, int[] values) {
        ConstraintSyntaxTree result = getVariable(vars[Math.abs(values[0]) - 1], values[0]);
        
        for (int i = 1; i < values.length; i++) {
            result = new OCLFeatureCall(result, OclKeyWords.OR, getVariable(vars[Math.abs(values[i]) - 1], values[i]));
        }
        
        return result;
    }
    
    private ConstraintSyntaxTree getVariable(Variable var, int value) {
        if (value < 0) {
            return new OCLFeatureCall(var, OclKeyWords.NOT);
        } else {
            return var;
        }
    }
    
}
