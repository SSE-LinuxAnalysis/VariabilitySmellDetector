package de.uni_hildesheim.sse.smell.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.util.ConditionUtils;
import de.uni_hildesheim.sse.smell.util.ConstraintException;
import de.uni_hildesheim.sse.smell.util.ICnfConverter;
import de.uni_hildesheim.sse.smell.util.RecursiveReplacingCnfConverter;
import de.uni_hildesheim.sse.smell.util.VarNotFoundException;
import de.uni_hildesheim.sse.smell.util.VariableToNumberConverter;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;

public abstract class AbstractSatSolverFilter implements IFilter {

    private VariableToNumberConverter varConverter;

    private ISolver solver;

    private String dimacsModelFile;
    
    private ICnfConverter cnfConverter;

    public AbstractSatSolverFilter(String dimacsModelFile) throws IOException {
        this.varConverter = new VariableToNumberConverter(dimacsModelFile, "CONFIG_"); // TODO: ENABLE_ for busybox 
        this.dimacsModelFile = dimacsModelFile;
        this.solver = getSolver(dimacsModelFile);
        if (solver == null) {
            throw new IOException("Solver is null!");
        }
        
        /*
         * Here, can the desired converter be chosen:
         * RecursiveReplacingCnfConverter is currently is the fastest,
         * which terminates in a reasonable time.
         */
        this.cnfConverter = new RecursiveReplacingCnfConverter(varConverter);
    }
    
    protected void addNewVariableToDimacsModel(String name) {
        varConverter.addVarible(name);
    }
    
    /**
     * @throws ConstraintException If constraint cannot be parsed.
     */
    protected boolean isSolvable(String constraint) throws VarNotFoundException, ConstraintException, TimeoutException {
        ConstraintSyntaxTree tree = ConditionUtils.parseCString(constraint);
        return isSolvable(tree);
    }
    
    
    protected boolean isSolvable(ConstraintSyntaxTree tree) throws VarNotFoundException, TimeoutException {
        try {
            return isSolvable(cnfConverter.convertToCnf(tree));
        } catch (ConstraintException e) {
            // this can't happen, since convertToCnf must return a CNF tree.
            throw new RuntimeException("Illegal state reached; CnfConverter is probably broken!");
        }
    }
    
    /**
     * @throws ConstraintException If cnfParts are not in CNF format.
     */
    protected boolean isSolvable(List<ConstraintSyntaxTree> cnfParts) throws VarNotFoundException, ConstraintException, TimeoutException {

        solver = getSolver(dimacsModelFile); // TODO: fix removing of constraints

        // List of constraints added to the solver.
        // This is needed since we want to clean the solver up after this
        // iteration.
//        List<IConstr> constraints = new ArrayList<>(); // TODO: fix removing of constraints

        try {
            // Temporarily (only for this iteration) add the constraint of the
            // smell candidate to the solver.
            // cleanUp() MUST BE CALLED BEFORE THE NEXT ITERATION!
            for (int i = 0; i < cnfParts.size(); i++) {
                // TODO: HACK
                int[] numbers = null;
                do {
                    try {
                        numbers = varConverter.convertToDimacs(cnfParts.get(i));
                    } catch (VarNotFoundException e) {
                        if (e.getName().startsWith("CONFIG_")) {
                            varConverter.addVarible(e.getName());
                            cnfParts.add(new OCLFeatureCall(new Variable(
                                    new DecisionVariableDeclaration(e.getName(), BooleanType.TYPE, null))
                                    , OclKeyWords.NOT));
                        } else {
                            throw e;
                        }
                    }
                } while (numbers == null);
                /*IConstr constr =*/ solver.addClause(new VecInt(numbers));
                // constr is null if the clause is a tautology
                // (see javadoc comment of org.sat4j.minisat.core.
                // DataStructureFactory.createClause(IVecInt literals))
//                if (constr != null) {
//                    constraints.add(constr);// TODO: fix removing of constraints
//                }
            }
        } catch (ContradictionException e) {
            // This is thrown if the model is already not solvable
//            cleanUp(constraints); // TODO: fix removing of constraints
            return false;
//        } catch (VarNotFoundException e) {
            // This is thrown if the code constraints contain variables that
            // are not found in the VarModel.
//            cleanUp(constraints); // TODO: fix removing of constraints
//            throw e;
        }

        boolean result = false;
        try {
            result = solver.isSatisfiable();
        } catch (TimeoutException e) {
//            cleanUp(constraints); // TODO: fix removing of constraints
            throw e;
        }
//        cleanUp(constraints); // TODO: fix removing of constraints
        return result;
    }

    /**
     * Reads the given DIMACS model file.
     * 
     * @param path
     *            The DIMACS model file.
     * @return A solver for the model.
     */
    private static ISolver getSolver(String path) {
        ISolver solver = SolverFactory.newDefault();
        solver.setDBSimplificationAllowed(false);
        Reader reader = new DimacsReader(solver);
        boolean successfulParsed = false;
        try {
            reader.parseInstance(path);
            successfulParsed = true;
        } catch (ParseFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        if (successfulParsed) {
            return solver;
        } else {
            return null;
        }
    }

    /**
     * Cleans up the solver by removing the temporary constraints added.
     * 
     * @param constraints
     *            The constraints that were added.
     */
//    private void cleanUp(List<IConstr> constraints) {
        // TODO: fix removing of constraints
//         for (IConstr constr : constraints) {
//              solver.removeConstr(constr);
//         }
//    }
    
    /**
     * Returns a part of a solution for the last {@link #isSolvable(ConstraintSyntaxTree)}
     * call.
     * 
     * @param vars The relevant variables; the values for these variables will be returned.
     * @return Only the relevant part of the solution of the VarModel that lead
     *      to a smell as a string.
     * @throws TimeoutException 
     */
    public Map<String, Boolean> getSolutionPart(Set<String> variables) throws VarNotFoundException, TimeoutException {
        int[] solution = solver.findModel();
        
        Map<Integer, String> relatedVarMapping = new HashMap<>((int) (variables.size() * 1.5));
        
        for (String var : variables) {
            relatedVarMapping.put(varConverter.getNumber(var), var);
        }
        
        Map<String, Boolean> result = new HashMap<>();
        
        for (int value : solution) {
            if (relatedVarMapping.containsKey(value)) {
                result.put(relatedVarMapping.get(value), true);
            } else if (relatedVarMapping.containsKey(-value)) {
                result.put(relatedVarMapping.get(-value), false);
            }
        }
        
        return result;
    }
    
    @Override
    public abstract List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws FilterException;

}
