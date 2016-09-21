package de.uni_hildesheim.sse.smell.filter.input;

import java.io.FileNotFoundException;

import de.uni_hildesheim.sse.smell.data.ConditionBlock;
import de.uni_hildesheim.sse.smell.data.ConditionType;
import de.uni_hildesheim.sse.smell.data.IDataElement;

/**
 * A filter that ignore all input and reads {@link ConditionBlock}s from a CSV file.
 * <br />
 * <b>Input</b>: nothing<br />
 * <b>Output</b>: {@link ConditionBlock}s<br />
 * 
 * @author Adam Krafczyk
 */
public class ConditionBlockReader extends AbstractCsvReaderFilter {

    private static final int FILENAME = 0;
    private static final int LINE_START = 1;
    private static final int LINE_END = 2;
    private static final int TYPE = 3;
    private static final int INDENTATION = 4;
    private static final int STARTING_IF = 5;
    private static final int CONDITION = 6;
    private static final int NORMALIZED = 7;
    
    /**
     * @param filename The file to read from.
     * @param skipFirstLine Whether the first line (possibly header) should be skipped.
     * @throws FileNotFoundException If the file cannot be opened.
     */
    public ConditionBlockReader(String filename, boolean skipFirstLine) throws FileNotFoundException {
        super(filename, skipFirstLine);
    }


    @Override
    public IDataElement readLine(String[] fields) {
        return new ConditionBlock(fields[FILENAME], Integer.parseInt(fields[LINE_START]),
                Integer.parseInt(fields[LINE_END]), ConditionType.getConditionType(fields[TYPE]),
                Integer.parseInt(fields[INDENTATION]), Integer.parseInt(fields[STARTING_IF]),
                fields[CONDITION], fields[NORMALIZED]);
    }
    
}
