package de.uni_hildesheim.sse.smell.filter.kaestraints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sat4j.specs.TimeoutException;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariablePresenceConditions;
import de.uni_hildesheim.sse.smell.data.VariableWithSolutions;
import de.uni_hildesheim.sse.smell.filter.AbstractSatSolverFilter;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.util.ConditionUtils;
import de.uni_hildesheim.sse.smell.util.ConstraintException;
import de.uni_hildesheim.sse.smell.util.RecursiveCnfConverter;
import de.uni_hildesheim.sse.smell.util.VarNotFoundException;
import de.uni_hildesheim.sse.smell.util.dnf.Solution;
import de.uni_hildesheim.sse.smell.util.dnf.SolutionFinder;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.filter.DeclrationInConstraintFinder;

class PcSmellDetectorWorker extends AbstractSatSolverFilter {

    public PcSmellDetectorWorker(String dimacsModelFile) throws IOException {
        super(dimacsModelFile);
        
        addNewVariableToDimacsModel(RecursiveCnfConverter.PSEUDO_TRUE.getVariable().getName());
        addNewVariableToDimacsModel(RecursiveCnfConverter.PSEUDO_FALSE.getVariable().getName());
    }

    public IDataElement runOnElement(VariablePresenceConditions pcs) {
        IDataElement smell = null;
        
        try {
            List<ConstraintSyntaxTree> xorTrees = new ArrayList<>();
            
            for (String pc : pcs.getPresenceConditions()) {
                ConstraintSyntaxTree cst = ConditionUtils.parseCString(pc);
                
                ConstraintSyntaxTree trueCst = NoDominatingFilter.setToValue(cst, pcs.getVariable(), true);
                ConstraintSyntaxTree falseCst = NoDominatingFilter.setToValue(cst, pcs.getVariable(), false);
                
//                xorTrees.add(new OCLFeatureCall(trueCst, OclKeyWords.XOR, falseCst));
                ConstraintSyntaxTree atLeastOnePositive = new OCLFeatureCall(trueCst, OclKeyWords.OR, falseCst);
                ConstraintSyntaxTree atLeastOneNegative = new OCLFeatureCall(
                        new OCLFeatureCall(trueCst, OclKeyWords.NOT),
                        OclKeyWords.OR,
                        new OCLFeatureCall(falseCst, OclKeyWords.NOT));
                // One of positive AND one of negative
                xorTrees.add(new OCLFeatureCall(atLeastOnePositive, OclKeyWords.AND, atLeastOneNegative));
            }
            
            // term_0 or term_1 or ...
            ConstraintSyntaxTree constr = xorTrees.get(0);
            for (int i = 1; i < xorTrees.size(); i++) {
                constr = new OCLFeatureCall(constr, OclKeyWords.OR, xorTrees.get(i));
            }
            
            constr = new OCLFeatureCall(constr, OclKeyWords.NOT);
            // A and Not(PCs of A)
            ConstraintSyntaxTree toCheck = new OCLFeatureCall(
                    ConditionUtils.parseCString(pcs.getVariable()), OclKeyWords.AND, constr);
            
            /*
             * Checks whether: VM -> (A -> PCs) is tautology
             * <=> VM AND A AND Not(PCs) is not satisfiable
             */
            boolean solvable = isSolvable(toCheck);
            
            /*
             * Smell detected, if satisfiable
             */
            if (solvable) {
                /*
                 * Exceptions will be thrown before this result is written,
                 * this avoids that partial validated results will be reported
                 * as false positive results (but reduces number of potential
                 * true results).
                 */
                smell = buildResult(pcs, constr);
            }
            
        } catch (VarNotFoundException e1) {
            // This is thrown if the code constraints contain variables that
            // are not found in the VarModel.
            System.out.println("Cannot solve candidate " + pcs.getVariable()
                    + ", because the following variable was not found in the DIMACS model: " + e1.getName());
        } catch (TimeoutException | ConstraintException e1) {
            System.out.println("Cannot solve candidate " + pcs.getVariable()
                    + ", because the following exception occured:");
            e1.printStackTrace(System.out);
        }
        
        return smell;
    }
    
    private IDataElement buildResult(VariablePresenceConditions pcs, ConstraintSyntaxTree constr) {
        // TODO:
        // Find all configurations, where constr is true
        // For all these configurations, remove the ones that are not SAT(VM && pcs.getVariable() && config)
        // All the remaining configurations are the problematic partial Kconfig configurations
        //
        // For example, if constr is !(B || C), and pcs.getVariable() is A
        // We already checked, that SAT(VM && A && !(B || C)) is true, so we have a smell
        // Now we want to find the problematic configurations where the smell takes affect
        // !(B || C) is true, iff B=false and C=false; so we have 1 problematic configuration
        // The meaning of this problematic configuration is, that A has no effect on the code if
        // B and C are both disabled
        
        DeclrationInConstraintFinder varFinder = new DeclrationInConstraintFinder(constr);
        int numVaraibles = varFinder.getDeclarations().size();

        VariableWithSolutions result = new VariableWithSolutions(pcs.getVariable());
        
        if (numVaraibles > 12) {
            System.out.println("Warning: Cannot find solutions for constraint for variable " + pcs.getVariable() + ", because it's too big");
            result.addSolution(new TooBigSolution());
            
        } else {
            SolutionFinder finder = new SolutionFinder();
            try {
                Set<Solution> solutions = finder.getSolutions(constr);
                
                if (solutions.size() == 0) {
                    System.out.println("Warning: Constraint for variable " + pcs.getVariable() + " has no solutions, although there should be solutions");
                }
                
                removeImpossibleConfigurations(ConditionUtils.parseCString(pcs.getVariable()), constr, solutions);
                
                result.addSolutions(solutions);
            } catch (ConstraintException e) {
                System.out.println("Cannot find solutions for constraint for variable " + pcs.getVariable() + ", because:");
                e.printStackTrace(System.out);
            }
            
        }
        
        return result;
    }
    
    private void removeImpossibleConfigurations(ConstraintSyntaxTree var, ConstraintSyntaxTree constr, Set<Solution> solutions) {
        ConstraintSyntaxTree varAndConstr = new OCLFeatureCall(var, OclKeyWords.AND, constr);
        
        Iterator<Solution> it = solutions.iterator();
        
        while (it.hasNext()) {
            Solution s = it.next();
            ConstraintSyntaxTree toCheck = new OCLFeatureCall(varAndConstr, OclKeyWords.AND, buildCstFromSolution(s));
            
            try {
                if (!isSolvable(toCheck)) {
                    it.remove();
                }
            } catch (VarNotFoundException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
    }
    
    private ConstraintSyntaxTree buildCstFromSolution(Solution solution) {
        Set<String> vars = solution.getVariables();
        Iterator<String> it = vars.iterator();
        
        String var = it.next();
        ConstraintSyntaxTree result = getVariable(var, !solution.getValue(var));
        while (it.hasNext()) {
            var = it.next();
            result = new OCLFeatureCall(result, OclKeyWords.AND, getVariable(var, !solution.getValue(var)));
        }
        
        return result;
    }
    
    private ConstraintSyntaxTree getVariable(String name, boolean negated) {
        // TODO: is it good here to create new instance of variables for vars with same name?
        Variable var = new Variable(new DecisionVariableDeclaration(name, BooleanType.TYPE, null));
        if (negated) {
            return new OCLFeatureCall(var, OclKeyWords.NOT);
        } else {
            return var;
        }
    }
    
    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws FilterException {
        return null;
    }

}
