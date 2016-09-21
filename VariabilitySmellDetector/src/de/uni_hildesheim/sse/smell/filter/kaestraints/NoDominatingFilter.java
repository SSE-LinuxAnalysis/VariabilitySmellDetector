package de.uni_hildesheim.sse.smell.filter.kaestraints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariablePresenceConditions;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;
import de.uni_hildesheim.sse.smell.util.ConditionUtils;
import de.uni_hildesheim.sse.smell.util.ConstraintException;
import de.uni_hildesheim.sse.trans.convert.DeclarationInConstraintFinderWithDepth;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.cstEvaluation.EvaluationVisitor;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.Constraint;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.values.BooleanValue;

/**
 * <b>Input</b>: {@link VariablePresenceConditions}s<br />
 * <b>Output</b>: {@link VariablePresenceConditions}s<br />
 * 
 * @author Adam Krafczyk
 */
public class NoDominatingFilter implements IFilter {

    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws FilterException {
        List<IDataElement> result = new ArrayList<IDataElement>();
        progressPrinter.start(this, data.size());
        
        for (IDataElement element : data) {
            if (!(element instanceof VariablePresenceConditions)) {
                throw new WrongFilterException(NoDominatingFilter.class, VariablePresenceConditions.class, element);
            }
            VariablePresenceConditions pcs = (VariablePresenceConditions) element;
            
            /* 
             * Sort: Shortest presence condition (PC) first, most complex last.
             * We expect that shorter PCs have higher probability to be
             * non dominating (i.e. true XOR false is true for them), thus
             * decreasing the number of PCs to check.
             */
            Collections.sort(pcs.getPresenceConditions(), new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return Integer.compare(o1.length(), o2.length());
                }
            });
            
            boolean hasNonDominating = false;
            
            for (String pc : pcs.getPresenceConditions()) {
                
                
                ConstraintSyntaxTree cst = null;
                ConstraintSyntaxTree trueCst = null;
                ConstraintSyntaxTree falseCst = null;
                try {
                    cst = ConditionUtils.parseCString(pc);
                    trueCst = setToValue(cst, pcs.getVariable(), true);
                    falseCst = setToValue(cst, pcs.getVariable(), false);
                } catch (ConstraintException e) {
                    System.out.println("Ignoring " + pcs.getVariable()
                            + ", because this presence condition cannot be parsed: "
                            + pc);
                    hasNonDominating = true;
                    break;
                }
                
                int r1 = checkConstraint(trueCst);
                int r2 = checkConstraint(falseCst);
                
                /* 
                 * Check that both constraints can be evaluated to a Boolean
                 * value (not undefined) and that the XOR connection of them is
                 * true.
                 */
                boolean bothDefined = r1 != -1 && r2 != -1;
                boolean xorPermanetlyTrue = r1 != r2;
                if (bothDefined && xorPermanetlyTrue) {
                    // Break if resulting disjunction term is permanently true
                    hasNonDominating = true;
                    break;
                }
            }
            
            if (!hasNonDominating) {
                result.add(pcs);
            }
            
            progressPrinter.finishedOne();
        }
        
        return result;
    }
    
    /**
     * Checks whether the constraint is already true, false or still undefined.
     * 
     * @param constraint The {@link ConstraintSyntaxTree} that represents the boolean expression
     * 
     * @return 0 if the expression is false; 1 if the expression is true; -1 if the expression is undefined
     */
    private int checkConstraint(ConstraintSyntaxTree constraint) {
        // Get an array of all variables in the constraint
        DeclarationInConstraintFinderWithDepth finder = new DeclarationInConstraintFinderWithDepth(constraint);
        AbstractVariable[] declarationArray = finder.getDeclarationsInOrder().toArray(new AbstractVariable[] {});      
        
        // Create a project which only contains our single Constraint
        Project singleConstraintProject = new Project("SingleConstraintProject");
        Constraint constraintCopy = null;
        try {
            constraintCopy = new Constraint(constraint, singleConstraintProject);
        } catch (CSTSemanticException e) {
            // Cannot happen
            e.printStackTrace();
        }
        singleConstraintProject.add(constraintCopy);
        for (AbstractVariable var : declarationArray) {
            singleConstraintProject.add(var);
        }
        
        // Create a configuration object for the singleConstraintProject
        Configuration config = new Configuration(singleConstraintProject);
        
        EvaluationVisitor evaluationVisitor = new EvaluationVisitor(config, null, false, null);
        
        constraint.accept(evaluationVisitor);
        
        int result = -1;
        if (evaluationVisitor.constraintFailed()) {
            result = 0;
        } else if (evaluationVisitor.constraintFulfilled()) {
            result = 1;
        }
        
        return result;
    }
    
    /**
     * Replaces (recursively) all occurrences of <tt>variable</tt> with the 
     * Boolean constant <tt>value</tt> in <tt>cst</tt>.
     * 
     * @param cst The constraint where to replace the the variable.
     *     Only {@link OCLFeatureCall}s and {@link Variable}s are allowed.
     * @param variable The name of the variable to replace
     * @param value The value to set
     * @return The exchanged constraint (copy)
     * @throws ConstraintException If <tt>cst</tt> contains unexpected
     *     elements.
     */
    public static ConstraintSyntaxTree setToValue(ConstraintSyntaxTree cst, String variable, boolean value) throws ConstraintException {
        if (cst instanceof OCLFeatureCall) {
            OCLFeatureCall call = (OCLFeatureCall) cst;
            ConstraintSyntaxTree operand = setToValue(call.getOperand(), variable, value);
            ConstraintSyntaxTree[] parameters = new ConstraintSyntaxTree[call.getParameterCount()];
            for (int i = 0; i < call.getParameterCount(); i++) {
                parameters[i] = setToValue(call.getParameter(i), variable, value);
            }
            
            return new OCLFeatureCall(operand, call.getOperation(), parameters);
        } else if (cst instanceof Variable) {
            AbstractVariable var = ((Variable) cst).getVariable();
            if (var.getName().equals(variable)) {
                return new ConstantValue(BooleanValue.toBooleanValue(value));
            }
            
            return cst;
        } else {
            throw new ConstraintException("Found unexpected element in CST: " + cst);
        }
    }

}
