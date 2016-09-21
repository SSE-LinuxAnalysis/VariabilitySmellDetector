package de.uni_hildesheim.sse.smell;

import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.IFilter;

/**
 * A pipeline that chains {@link IFilter}s together. First set up the pipeline
 * via {@link #addFilter(IFilter)} calls, then start it via {@link #run()}.
 * The return value of each filter is passed to the next filer in the pipeline.
 * Usually, the first filter should create / read data, the intermediate filters
 * should process it, and the last filter should print it somewhere.
 *  
 * @author Adam Krafczyk
 */
public class Pipeline {

    private List<IFilter> filter;
    
    private IProgressPrinter progressPrinter;
    
    /**
     * Creates a new {@link Pipeline}.
     * 
     * @param progressPrinter The {@link IProgressPrinter} for this pipeline.
     */
    public Pipeline(IProgressPrinter progressPrinter) {
        filter = new ArrayList<>();
        this.progressPrinter = progressPrinter;
    }
    
    /**
     * Adds a filter to the end of the pipeline.
     * 
     * @param filter The filter to add.
     */
    public void addFilter(IFilter filter) {
        this.filter.add(filter);
    }
    
    /**
     * Runs all added filters in the order they were added.
     * 
     * @throws FilterException If a filter is called with invalid data.
     */
    public void run() throws FilterException {
        List<IDataElement> data = null;
        for (IFilter filter : filter) {
            data = filter.run(data, progressPrinter);
        }
    }
    
}
