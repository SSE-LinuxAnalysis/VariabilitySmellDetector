package de.uni_hildesheim.sse.smell;

import java.io.PrintStream;

/**
 * Prints the progress of a filter to the console.
 * 
 * @author Adam Krafczyk
 */
public class StreamProgressPrinter implements IProgressPrinter {

    private static final long PRINT_EVERY = 2000;
    
    private PrintStream out;
    
    private String currentFilter;
    
    private int estimatedCount;
    
    private int finished;
    
    private long lastcall;
    
    private long startTime;
    
    public StreamProgressPrinter() {
        this(System.out);
    }
    
    public StreamProgressPrinter(PrintStream out) {
        this.out = out;
    }
    
    @Override
    public void start(String filtername, int estimatedCount) {
        currentFilter = filtername;
        this.estimatedCount = estimatedCount;
        finished = 0;
        if (estimatedCount > 0) {
            out.println("Filter " + currentFilter + " started on " + estimatedCount + " elements");
        } else {
            out.println("Filter " + currentFilter + " started");
        }
        lastcall = System.currentTimeMillis();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void finishedOne() {
        finished++;
        if (System.currentTimeMillis() - lastcall > PRINT_EVERY || estimatedCount == finished) {
            lastcall = System.currentTimeMillis();
            if (estimatedCount > 0) {
                double percent = (double) finished / estimatedCount;
                
                long totalElapsed = System.currentTimeMillis() - startTime;
                double eta = totalElapsed / percent - totalElapsed;
                
                out.println("Filter " + currentFilter + " "
                        +(int) (percent * 100) + "% done ("
                        + finished + "/" + estimatedCount + "; ETA " + ((long) eta / 1000) + "s)");
            } else {
                out.println("Filter " + currentFilter + " finished " + finished);
            }
        }
    }

    @Override
    public void start(Object filter, int estimatedCount) {
        start(filter.getClass().getSimpleName(), estimatedCount);
    }
    
}
