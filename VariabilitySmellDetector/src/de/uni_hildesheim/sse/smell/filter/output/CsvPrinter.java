package de.uni_hildesheim.sse.smell.filter.output;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;

/**
 * A filter that prints out the data in CSV format.
 * <br />
 * <b>Input</b>: {@link IDataElement}s<br />
 * <b>Output</b>: {@link IDataElement}s<br />
 * 
 * @author Adam Krafczyk
 */
public class CsvPrinter implements IFilter {

    private PrintWriter out;
    
    /**
     * @param out The stream to write the CSV output to.
     */
    public CsvPrinter(PrintStream out) {
        this.out = new PrintWriter(out);
    }
    
    /**
     * @param out The writer to write the CSV output to.
     */
    public CsvPrinter(PrintWriter out) {
        this.out = out;
    }
    
    /**
     * @param filename The filename to write the CSV output to.
     * @throws FileNotFoundException If the file cannot be opened for writing.
     */
    public CsvPrinter(String filename) throws FileNotFoundException {
        this(new PrintWriter(filename));
    }

    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws WrongFilterException {
        progressPrinter.start("CsvPrinter", data.size());
        if (!data.isEmpty()) {
            // Write header
            IDataElement element = data.get(0);
            out.println(element.headertoCsvLine());
            
            // Write Data
            out.println(element.toCsvLine());
            progressPrinter.finishedOne();
            for (int i = 1; i < data.size(); i++) {
                element = data.get(i);
                out.println(element.toCsvLine());
                progressPrinter.finishedOne();
            }
        }
        out.close();
        return data;
    }
    
}
