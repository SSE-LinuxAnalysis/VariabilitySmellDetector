package de.uni_hildesheim.sse.smell.filter.output;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariablePresenceConditions;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;

/**
 * A filter that prints out the data in CSV format and discards it, to save memory.
 * <br />
 * <b>Input</b>: {@link IDataElement}s<br />
 * <b>Output</b>: {@link IDataElement}s<br />
 * 
 * @author Adam Krafczyk
 */
public class CsvDumper implements IFilter {

    private PrintWriter out;
    
    /**
     * @param out The stream to write the CSV output to.
     */
    public CsvDumper(PrintStream out) {
        this.out = new PrintWriter(out);
    }
    
    /**
     * @param out The writer to write the CSV output to.
     */
    public CsvDumper(PrintWriter out) {
        this.out = out;
    }
    
    /**
     * @param filename The filename to write the CSV output to.
     * @throws FileNotFoundException If the file cannot be opened for writing.
     */
    public CsvDumper(String filename) throws FileNotFoundException {
        this(new PrintWriter(filename));
    }

    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws WrongFilterException {
        progressPrinter.start("CsvDumper", data.size());
        if (!data.isEmpty()) {
            // Write header
            VariablePresenceConditions element = (VariablePresenceConditions) data.get(0);
            data.set(0, null);
            out.println(element.headertoCsvLine());
            
            // Write Data
//            out.println(element.toCsvLine());
            element.printCsvLine(out);
            
            progressPrinter.finishedOne();
            for (int i = 1; i < data.size(); i++) {
                element = (VariablePresenceConditions) data.get(i);
                data.set(i, null);
//                out.println(element.toCsvLine());
                element.printCsvLine(out);
                progressPrinter.finishedOne();
            }
        }
        out.close();
        return data;
    }
    
}
