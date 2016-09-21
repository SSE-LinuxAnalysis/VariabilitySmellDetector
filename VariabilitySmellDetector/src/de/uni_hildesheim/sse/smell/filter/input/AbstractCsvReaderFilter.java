package de.uni_hildesheim.sse.smell.filter.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.IFilter;

public abstract class AbstractCsvReaderFilter implements IFilter {

    private static final String DEFAULT_DELIMITER = ";";
    
    private File file;
    
    private boolean skipFirstLine;
    
    private String delimiter;
    
    public AbstractCsvReaderFilter(String path, boolean skipFirstLine) throws FileNotFoundException {
        this(path, DEFAULT_DELIMITER, skipFirstLine);
    }

    public AbstractCsvReaderFilter(String path, String delimiter, boolean skipFirstLine) throws FileNotFoundException {
        file = new File(path);
        this.delimiter = delimiter;
        if (null == file || !file.exists()) {
            throw new FileNotFoundException("File \"" + path + "\" does not exist.");
        }
        this.skipFirstLine = skipFirstLine;
    }
    
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws FilterException {
        List<IDataElement> result = new LinkedList<>();
        
        progressPrinter.start(this, 0);
        
        // Open file if reading starts and not when constructor is called.
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            String line;
            if (skipFirstLine) {
                in.readLine();
            }
            while ((line = in.readLine()) != null) {
                String[] fields = line.split(delimiter);
                result.add(readLine(fields));
                
                progressPrinter.finishedOne();
            }
        } catch (IOException e) {
            throw new FilterException(e);
        } finally {
            if  (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        
        return result;
    }
    
    public abstract IDataElement readLine(String[] fields);
}
