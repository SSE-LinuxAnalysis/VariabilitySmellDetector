package de.uni_hildesheim.sse.smell.filter.old_permanent_parent;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.sat4j.specs.TimeoutException;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.PermanentNestedSmellCandidate;
import de.uni_hildesheim.sse.smell.filter.AbstractSatSolverFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;
import de.uni_hildesheim.sse.smell.util.ConstraintException;
import de.uni_hildesheim.sse.smell.util.VarNotFoundException;

/**
 * This is a {@link SmellDetector} that additionally checks, whether variables
 * are configurable by hand.
 * 
 * @author Adam Krafczyk
 */
public class PaperSmellDetector extends AbstractSatSolverFilter {

    private PrintWriter missingVars;

    public PaperSmellDetector(String dimacsModelFile, String missingVarsFile) throws IOException {
        super(dimacsModelFile);
        if (missingVarsFile != null) {
            this.missingVars = new PrintWriter(missingVarsFile);
        }
    }

    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter)
            throws WrongFilterException {
        List<IDataElement> result = new ArrayList<>();

        progressPrinter.start(this, data.size());

        for (IDataElement element : data) {

            if (!(element instanceof PermanentNestedSmellCandidate)) {
                throw new WrongFilterException(SmellDetector3.class, PermanentNestedSmellCandidate.class, data);
            }
            PermanentNestedSmellCandidate candidate = (PermanentNestedSmellCandidate) element;

            try {
                String child = candidate.getPermanentNestedVar();
                String parent = candidate.getOuterCondition();
                
                boolean notChildAndNotParent = isSolvable("!(" + child + ") && !(" + parent + ")");
                boolean childAndNotParent = isSolvable("(" + child + ") && !(" + parent + ")");
                
                if (notChildAndNotParent && childAndNotParent) {
                    result.add(candidate.getSmell(this));
                }
                
            } catch (VarNotFoundException e1) {
                // This is thrown if the code constraints contain variables that
                // are not found in the VarModel.
                if (missingVars != null) {
                    missingVars.println(e1.getName());
                }
                System.out.println("Cannot solve candidate " + candidate.getPermanentNestedVar()
                        + ", because the following variable was not found in the DIMACS model: " + e1.getName());
            } catch (TimeoutException | ConstraintException e1) {
                System.out.println("Cannot solve candidate " + candidate.getPermanentNestedVar()
                        + ", because the following exception occured:");
                e1.printStackTrace(System.out);
            }

            progressPrinter.finishedOne();
        }
        return result;
    }

}
