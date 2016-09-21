package de.uni_hildesheim.sse.smell.filter.input;

import java.io.FileNotFoundException;

import de.uni_hildesheim.sse.smell.data.ConditionBlock;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariableWithSolutions;
import de.uni_hildesheim.sse.smell.util.dnf.Solution;

/**
 * A filter that ignore all input and reads {@link ConditionBlock}s from a CSV file.
 * <br />
 * <b>Input</b>: nothing<br />
 * <b>Output</b>: {@link VariableWithSolutions}s<br />
 * 
 * @author Adam Krafczyk
 */
public class VariableWithSolutionsReader extends AbstractCsvReaderFilter {

    /**
     * @param filename The file to read from.
     * @param skipFirstLine Whether the first line (possibly header) should be skipped.
     * @throws FileNotFoundException If the file cannot be opened.
     */
    public VariableWithSolutionsReader(String filename, boolean skipFirstLine) throws FileNotFoundException {
        super(filename, skipFirstLine);
    }

    @Override
    public IDataElement readLine(String[] fields) {
        VariableWithSolutions var = new VariableWithSolutions(fields[0]);
        
        for (int i = 1; i < fields.length; i++) {
            var.addSolution(readSolution(fields[i]));
        }
        return var;
    }
    
    private Solution readSolution(String solutionString) {
        String[] vars = solutionString.split(" ");
        
        Solution result = new Solution();
        
        for (String var : vars) {
            boolean negated = var.startsWith("!");
            if (negated) {
                var = var.substring(1);
            }
            result.setValue(var, !negated);
        }
        
        return result;
    }

    
}
