package de.uni_hildesheim.sse.smell;

import java.io.IOException;

import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.input.VariablePresenceConditionsReader;
import de.uni_hildesheim.sse.smell.filter.kaestraints.NoDominatingFilter;
import de.uni_hildesheim.sse.smell.filter.kaestraints.PcSmellDetector;
import de.uni_hildesheim.sse.smell.filter.output.CsvPrinter;

public class KaestraintsMain {

    public static void main(String[] args) throws IOException, FilterException {
        Pipeline pipeline = new Pipeline(new StreamProgressPrinter());
        
        String pcPath = args[0];
        String dimacsPath = args[1];
        String result = args[2];
        
        pipeline.addFilter(new VariablePresenceConditionsReader(pcPath, false));
        
        pipeline.addFilter(new NoDominatingFilter());
        pipeline.addFilter(new PcSmellDetector(dimacsPath));
        
        pipeline.addFilter(new CsvPrinter(result));
        
        pipeline.run();
    }
    
}
