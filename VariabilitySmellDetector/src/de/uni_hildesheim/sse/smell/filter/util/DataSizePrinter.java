package de.uni_hildesheim.sse.smell.filter.util;

import java.util.List;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.IFilter;

/**
 * <b>Input</b>: {@link IDataElement}s<br />
 * <b>Output</b>: {@link IDataElement}s<br />
 * 
 * @author Adam Krafczyk
 */
public class DataSizePrinter implements IFilter {

    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws FilterException {
        progressPrinter.start(this, 1);
        String type = "<empty>";
        if (!data.isEmpty()) {
            type = data.get(0).getClass().getCanonicalName();
        }
        System.out.println("The data set has " + data.size() + " elements of type " + type);
        progressPrinter.finishedOne();
        return data;
    }

}
