package de.uni_hildesheim.sse.smell.test.filter.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.smell.NullProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariableWithSolutions;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.util.RedundantSolutionFilter;
import de.uni_hildesheim.sse.smell.util.dnf.Solution;

public class RedundantSolutionFilterTest {

    @Test
    public void testSimple() throws Exception {
       Solution s1 = new Solution();
       Solution s2 = new Solution();
       
       s1.setValue("A", true);
       s1.setValue("B", false);
       s1.setValue("C", false);
       
       s2.setValue("A", true);
       s2.setValue("B", true);
       s2.setValue("C", false);
       
       List<Solution> result = runOnSolutions(s1, s2);
       
       Assert.assertEquals(1, result.size());
       
       Solution e1 = new Solution();
       e1.setValue("A", true);
       e1.setValue("C", false);
       
       testContainsSolution(result, e1);
    }
    
    @Test
    public void testNoSimplification() throws Exception {
        Solution s1 = new Solution();
        Solution s2 = new Solution();
        
        s1.setValue("A", true);
        s1.setValue("B", false);
        s1.setValue("C", false);
        
        s2.setValue("A", false);
        s2.setValue("B", true);
        s2.setValue("C", false);
        
        List<Solution> result = runOnSolutions(s1, s2);
        
        Assert.assertEquals(2, result.size());
        
        testContainsSolution(result, s1);
        testContainsSolution(result, s2);
    }
    
    @Test
    public void testMultiStep() throws Exception {
        Solution s1 = new Solution();
        Solution s2 = new Solution();
        Solution s3 = new Solution();
        
        s1.setValue("A", true);
        s1.setValue("B", false);
        s1.setValue("C", false);
        
        s2.setValue("A", true);
        s2.setValue("B", true);
        s2.setValue("C", false);
        
        s3.setValue("A", false);
        s3.setValue("C", false);
        
        List<Solution> result = runOnSolutions(s1, s3, s2);
        
        Assert.assertEquals(1, result.size());
        
        Solution e1 = new Solution();
        e1.setValue("C", false);
        testContainsSolution(result, e1);
    }
    
    @Test
    public void testDuplicates() throws Exception {
        Solution s1 = new Solution();
        Solution s2 = new Solution();
        
        s1.setValue("A", true);
        s1.setValue("B", false);
        s1.setValue("C", false);
        
        s2.setValue("A", true);
        s2.setValue("B", false);
        s2.setValue("C", false);
        
        List<Solution> result = runOnSolutions(s1, s2);
        
        Assert.assertEquals(1, result.size());
        testContainsSolution(result, s1);
    }
    
    private void testContainsSolution(List<Solution> result, Solution expected) {
        for (Solution s : result) {
            if (s.equals(expected)) {
                return;
            }
        }
        Assert.fail("Couldn't find solution: " + expected);
    }
    
    private List<Solution> runOnSolutions(Solution ...solutions) throws FilterException {
        RedundantSolutionFilter filter = new RedundantSolutionFilter();
        
        VariableWithSolutions dataElementIn = new VariableWithSolutions("A");
        for (Solution s : solutions) {
            dataElementIn.addSolution(s);
        }
        
        List<IDataElement> data = new ArrayList<>();
        data.add(dataElementIn);
        
        data = filter.run(data, new NullProgressPrinter());
        
        Assert.assertEquals(1, data.size());
        
        VariableWithSolutions dataElementOut = (VariableWithSolutions) data.get(0);
        
        return dataElementOut.getSolutions();
    }
    
}
