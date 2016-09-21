package de.uni_hildesheim.sse.smell.filter.input;

import java.io.FileNotFoundException;

import de.uni_hildesheim.sse.smell.data.ConditionBlock;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariablePresenceConditions;

/**
 * A filter that ignore all input and reads {@link ConditionBlock}s from a CSV file.
 * <br />
 * <b>Input</b>: nothing<br />
 * <b>Output</b>: {@link VariablePresenceConditions}s<br />
 * 
 * @author Adam Krafczyk
 */
public class VariablePresenceConditionsReader extends AbstractCsvReaderFilter {

    /**
     * @param filename The file to read from.
     * @param skipFirstLine Whether the first line (possibly header) should be skipped.
     * @throws FileNotFoundException If the file cannot be opened.
     */
    public VariablePresenceConditionsReader(String filename, boolean skipFirstLine) throws FileNotFoundException {
        super(filename, skipFirstLine);
    }

    @Override
    public IDataElement readLine(String[] fields) {
        VariablePresenceConditions pc = new VariablePresenceConditions(fields[0]);
        
        for (int i = 1; i < fields.length; i++) {
            pc.addPresenceCondition(fields[i]);
        }
        return pc;
    }

    
}
