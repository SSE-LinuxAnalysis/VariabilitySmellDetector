package de.uni_hildesheim.sse.smell.filter.kaestraints;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariablePresenceConditions;
import de.uni_hildesheim.sse.smell.data.VariableWithSolutions;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;

/**
 * <b>Input</b>: {@link VariablePresenceConditions}s<br />
 * <b>Output</b>: {@link VariableWithSolutions}s<br />
 * 
 * @author Adam Krafczyk
 */
public class PcSmellDetector implements IFilter {

    private PrintWriter prematureOutput;
    
    private boolean headerWritten;
    
    private String dimacsModelFile;
    
    private int numWorkers;
    
    private List<IDataElement> result;
    
    private IProgressPrinter progressPrinter;
    
    public PcSmellDetector(String dimacsModelFile, String prematureOutput, int numWorkers) throws IOException {
        this(dimacsModelFile, numWorkers);
        this.prematureOutput = new PrintWriter(prematureOutput);
    }
    
    public PcSmellDetector(String dimacsModelFile, int numWorkers) throws IOException {
        this.numWorkers = numWorkers;
        this.dimacsModelFile = dimacsModelFile;
    }
    
    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter)
            throws WrongFilterException {
        
        result = new ArrayList<>(300);

        this.progressPrinter = progressPrinter;
        progressPrinter.start(this, data.size());
        
        /* 
         * Sort: By number of presence conditions per variable (increasing),
         * should not affect the algorithm, is only for readability and
         * delivers results faster
         */
        Collections.sort(data, new Comparator<IDataElement>() {

            @Override
            public int compare(IDataElement o1, IDataElement o2) {
                VariablePresenceConditions p1 = (VariablePresenceConditions) o1;
                VariablePresenceConditions p2 = (VariablePresenceConditions) o2;
                return Integer.compare(p1.getPresenceConditions().size(), p2.getPresenceConditions().size());
            }
        });

        final ConcurrentLinkedQueue<VariablePresenceConditions> toProcess = new ConcurrentLinkedQueue<>();
        
        for (IDataElement element : data) {

            if (!(element instanceof VariablePresenceConditions)) {
                throw new WrongFilterException(PcSmellDetector.class, VariablePresenceConditions.class, data);
            }
            VariablePresenceConditions pcs = (VariablePresenceConditions) element;

           toProcess.add(pcs);
        }
        
        try {
            List<Thread> workers = new ArrayList<>();
            
            for (int i = 0; i < numWorkers; i++) {
                final PcSmellDetectorWorker worker = new PcSmellDetectorWorker(dimacsModelFile);
                
                workers.add(new Thread() {
                    
                    @Override
                    public void run() {
                        boolean run = true;
                        
                        while (run) {
                            
                            VariablePresenceConditions pcs = toProcess.poll();
                            
                            if (pcs != null) {
                                onResult(worker.runOnElement(pcs));
                                
                            } else {
                                run = false;
                            }
                            
                        }
                    };
                    
                });
            }
            

            for (Thread worker : workers) {
                worker.start();
            }
            
            for (Thread worker : workers) {
                try {
                    worker.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (prematureOutput != null) {
            prematureOutput.close();
        }
        
        return result;
    }
    
    private synchronized void onResult(IDataElement smell) {
        if (smell != null) {
            result.add(smell);
            
            if (prematureOutput != null) {
                if (!headerWritten) {
                    prematureOutput.println(smell.headertoCsvLine());
                    headerWritten = true;
                }
                prematureOutput.println(smell.toCsvLine());
                prematureOutput.flush();
            }
        }
        
        progressPrinter.finishedOne();
    }

}
