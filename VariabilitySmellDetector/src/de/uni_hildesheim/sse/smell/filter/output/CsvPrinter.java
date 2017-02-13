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
    
    private boolean writeHeader;
    
    private boolean close;
    
    /**
     * @param out The stream to write the CSV output to.
     */
    public CsvPrinter(PrintStream out) {
        this(new PrintWriter(out));
    }
    
    public CsvPrinter(PrintStream out, boolean writeHeader, boolean close) {
        this(new PrintWriter(out), writeHeader, close);
    }
    
    /**
     * @param out The writer to write the CSV output to.
     */
    public CsvPrinter(PrintWriter out) {
        this(out, true, true);
    }
    
    public CsvPrinter(PrintWriter out, boolean writeHeader, boolean close) {
        this.out = out;
        this.writeHeader = writeHeader;
        this.close = close;
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
            IDataElement element = data.get(0);
            if (writeHeader) {
                // Write header
                out.println(element.headertoCsvLine());
            }
            
            // Write Data
            out.println(element.toCsvLine());
            progressPrinter.finishedOne();
            for (int i = 1; i < data.size(); i++) {
                element = data.get(i);
                out.println(element.toCsvLine());
                progressPrinter.finishedOne();
            }
        }
        if (close) {
            out.close();
        } else {
            out.flush();
        }
        return data;
    }
    
}
