package de.uni_hildesheim.sse.smell.test.util.dnf;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.smell.util.dnf.Solution;

public class SolutionTest {
    
    @Test
    public void testSimple() {
        Solution s = new Solution();
        
        Assert.assertEquals(0, s.getVariables().size());
        s.setValue("A", true);
        Assert.assertEquals(1, s.getVariables().size());
        s.setValue("B", true);
        Assert.assertEquals(2, s.getVariables().size());
        s.setValue("A", false);
        Assert.assertEquals(2, s.getVariables().size());
        
        Assert.assertEquals(true, s.getValue("B"));
        Assert.assertEquals(false, s.getValue("A"));
        
        Assert.assertTrue(s.getVariables().contains("A"));
        Assert.assertTrue(s.getVariables().contains("B"));
        
        s.removeValue("B");
        Assert.assertEquals(1, s.getVariables().size());
        Assert.assertFalse(s.getVariables().contains("B"));
    }
    
    @Test
    public void testEquals() {
        Solution s1 = new Solution();
        Solution s2 = new Solution();
        Assert.assertTrue(s1.equals(s2));
        Assert.assertTrue(s1.hasSameVariables(s2));
        
        s1.setValue("A", true);
        Assert.assertFalse(s1.equals(s2));
        Assert.assertFalse(s1.hasSameVariables(s2));
        
        s2.setValue("A", false);
        Assert.assertFalse(s1.equals(s2));
        Assert.assertTrue(s1.hasSameVariables(s2));
        
        s2.setValue("A", true);
        Assert.assertTrue(s1.equals(s2));
        Assert.assertTrue(s1.hasSameVariables(s2));
        
        s2.setValue("B", false);
        Assert.assertFalse(s1.equals(s2));
        Assert.assertFalse(s1.hasSameVariables(s2));
        
        s1.setValue("B", true);
        Assert.assertFalse(s1.equals(s2));
        Assert.assertTrue(s1.hasSameVariables(s2));
        
        s1.setValue("B", false);
        Assert.assertTrue(s1.equals(s2));
        Assert.assertTrue(s1.hasSameVariables(s2));
    }
    
    @Test
    public void testGetDifference() {
        Solution s1 = new Solution();
        Solution s2 = new Solution();
        
        s1.setValue("A", true);
        s1.setValue("B", true);
        s1.setValue("C", false);
        
        s2.setValue("A", false);
        s2.setValue("B", false);
        s2.setValue("C", true);
        
        Set<String> diff = s1.getDifference(s2);
        Assert.assertEquals(3, diff.size());
        Assert.assertTrue(diff.contains("A"));
        Assert.assertTrue(diff.contains("B"));
        Assert.assertTrue(diff.contains("C"));
        
        s2.setValue("B", true);
        diff = s1.getDifference(s2);
        Assert.assertEquals(2, diff.size());
        Assert.assertTrue(diff.contains("A"));
        Assert.assertTrue(diff.contains("C"));
        
        s2.setValue("C", false);
        diff = s1.getDifference(s2);
        Assert.assertEquals(1, diff.size());
        Assert.assertTrue(diff.contains("A"));
        
        s1.setValue("A", false);
        diff = s1.getDifference(s2);
        Assert.assertEquals(0, diff.size());
    }
    
    @Test
    public void testToString() {
        Solution s = new Solution();
        
        s.setValue("A", true);
        s.setValue("B", false);
        s.setValue("C", true);
        s.setValue("D", false);
        
        // surround with spaces so we can test easier
        String str = " " +  s.toString() + " ";
        
        Assert.assertTrue(str.contains(" A "));
        Assert.assertFalse(str.contains(" !A "));
        
        Assert.assertFalse(str.contains(" B "));
        Assert.assertTrue(str.contains(" !B "));
        
        Assert.assertTrue(str.contains(" C "));
        Assert.assertFalse(str.contains(" !C "));
        
        Assert.assertFalse(str.contains(" D "));
        Assert.assertTrue(str.contains(" !D "));
    }

}
