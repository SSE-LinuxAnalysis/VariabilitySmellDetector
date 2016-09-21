package de.uni_hildesheim.sse.smell.filter;

import java.util.List;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.Pipeline;
import de.uni_hildesheim.sse.smell.data.IDataElement;

/**
 * A filter that can be part of a {@link Pipeline} to process data.
 * 
 * @author Adam Krafczyk
 */
public interface IFilter {

    /**
     * Executes this filter on the given data.
     * 
     * @param data The data to process.
     * @param progressPrinter The {@link IProgressPrinter} to report progress to.
     * @return The processed data.
     * @throws WrongFilterException If the given data is not suitable for this filter.
     */
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws FilterException;
    
}
