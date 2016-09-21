package de.uni_hildesheim.sse.smell;

import de.uni_hildesheim.sse.smell.filter.IFilter;

/**
 * {@link IFilter}s will report their progress to a {@link IProgressPrinter}.
 * 
 * @author Adam Krafczyk
 */
public interface IProgressPrinter {

    /**
     * Called by a filter before any data is processed.
     * 
     * @param filtername The name of the filter that will run.
     * @param estimatedCount The estimated number of items to process.
     * <= 0 if not assessable.
     */
    public void start(String filtername, int estimatedCount);
   
    /**
     * Called by a filter before any data is processed.
     * 
     * @param filter The filter that will run.
     * @param estimatedCount The estimated number of items to process.
     * <= 0 if not assessable.
     */
    public void start(Object filter, int estimatedCount);
    
    /**
     * Called by the filter that last called {@link #start(Object, int)} or
     * {@link #start(String, int)} each time a single item is finished.
     */
    public void finishedOne();
    
}
