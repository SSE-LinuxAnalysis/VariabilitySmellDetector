package de.uni_hildesheim.sse.smell.filter.old_permanent_parent;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.sat4j.specs.TimeoutException;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.ISmell;
import de.uni_hildesheim.sse.smell.data.ISmellCandidate;
import de.uni_hildesheim.sse.smell.data.NestedDependsSmellCandidate;
import de.uni_hildesheim.sse.smell.filter.AbstractSatSolverFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;
import de.uni_hildesheim.sse.smell.util.ConstraintException;
import de.uni_hildesheim.sse.smell.util.VarNotFoundException;

/**
 * A filter that converts {@link ISmellCandidate}s into {@link ISmell}s by
 * checking whether a code dependency (see {@link ISmellCandidate#getVarModelCondition()}
 * reduces the number of legal configurations related to configurations
 * facilitated by the variability model.
 * <br />
 * <b>Input</b>: {@link ISmellCandidate}s<br />
 * <b>Output</b>: {@link ISmell}s<br />
 * 
 * @author Adam Krafczyk
 */
public class SmellDetector extends AbstractSatSolverFilter {

    private PrintWriter missingVars;
    
    /**
     * Creates a {@link SmellDetector} for the given VarModel.
     * 
     * @param dimacsModelFile A file containing the VarModel in DIMACS format.
     * @param missingVarsFile A filename to write variable names to, which are
     *      found in constraints in the code but not in the VarModel. <code>null</code>
     *      to disable.
     * @throws IOException If reading the model file fails.
     */
    public SmellDetector(String dimacsModelFile, String missingVarsFile) throws IOException {
        super(dimacsModelFile);
        if (missingVarsFile != null) {
            this.missingVars = new PrintWriter(missingVarsFile);
        }
    }
    
    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws WrongFilterException {
        List<IDataElement> result = new ArrayList<>();
        
        progressPrinter.start(this, data.size());
        
        for (IDataElement element : data) {
            
            if (!(element instanceof NestedDependsSmellCandidate)) {
                throw new WrongFilterException(SmellDetector.class, NestedDependsSmellCandidate.class,
                    data);
            }
            ISmellCandidate candidate = (ISmellCandidate) element;
            
            try {
                // If VarModel AND NOT(codeExtract) is solvable, then we have a smell.
                if (isSolvable(candidate.getVarModelCondition())) {
                    result.add(candidate.getSmell(this));
                }
            } catch (TimeoutException | ConstraintException e) {
                System.out.println("Cannot solve candidate " + candidate
                        + ", because the following exception occured:");
                e.printStackTrace(System.out);
            } catch (VarNotFoundException e) {
                if (missingVars != null) {
                    missingVars.println(e.getName());
                }
                System.out.println("Cannot solve candidate " + candidate
                    + ", because the following variable was not found in the DIMACS model: " + e.getName());
            }
            progressPrinter.finishedOne();
        }
        return result;
    }
    
}
