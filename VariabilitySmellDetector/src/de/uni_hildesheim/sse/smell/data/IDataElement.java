package de.uni_hildesheim.sse.smell.data;

import de.uni_hildesheim.sse.smell.filter.IFilter;

/**
 * A data element that will be passed to {@link IFilter}s.
 * 
 * @author Adam Krafczyk
 */
public interface IDataElement {

    /**
     * @param delim The delimiter of the new CSV line (usually ';').
     * @return This condition block as a CSV line with the given delimiter.
     */
    public String toCsvLine(String delim);
    
    /**
     * @return This conditon block as a CSV line with the default delimiter.
     */
    public String toCsvLine();
    
    /**
     * @return A CSV line that describes the columns returned by
     *      {@link #toCsvLine()} with the default delimiter.
     */
    public String headertoCsvLine();
    
    /**
     * @param delim The delimiter of the new CSV line.
     * @return A CSV line that describes the columns returned by {@link #toCsvLine()}.
     */
    public String headertoCsvLine(String delim);
    
}
