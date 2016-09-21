package de.uni_hildesheim.sse.smell.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_hildesheim.sse.model_extender.convert.ConstraintParser;
import de.uni_hildesheim.sse.model_extender.convert.ConstraintParserException;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;

/**
 * Utility methods for condition strings.
 * 
 * @author Adam Krafczyk
 * @author Sascha El-Sharkawy
 */
public class ConditionUtils {
    
    private static boolean isInteger(String s) {
        try { 
            Integer.parseInt(s); 
        } catch(NumberFormatException e) { 
            return false; 
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
    
    /**
     * Collects all variables found in the given condition.
     * TODO
     * 
     * @param condition The condition to parse.
     * @return A set of all variables.
     */
    public static Set<String> getVariables(String condition) {
        Pattern pattern = Pattern.compile("([A-Za-z_0-9]+)");
        Matcher matcher = pattern.matcher(condition);
        HashSet<String> vars = new HashSet<String>();
        while (matcher.find()) {
            if (!isInteger(matcher.group())) {
                vars.add(matcher.group());
            }
        }
        return vars;
    }
    
    /**
     * Converts the given formula into a {@link ConstraintSyntaxTree}. The formula
     * is in C notation; it can only contain &&, ||, !, brackets and variables.
     * 
     * @param formula The formula to convert.
     * @return The {@link ConstraintSyntaxTree} representing the condition.
     * 
     * @throws ConstraintException If parsing the constraint fails.
     */
    public static ConstraintSyntaxTree parseCString(String formula) throws ConstraintException {
        ConstraintSyntaxTree tree = null;
        try {
            tree = ConstraintParser.parse(formula, new DummyVariableSource());
        } catch (ConstraintParserException e1) {
            throw new ConstraintException(e1);
        }
        
        return tree;
    }
    
    /**
     * Reverse operation to {@link #parseCString(String)}. This converts a CST into a
     * String, using the C syntax. The constraint may only contain AND, OR, NOT
     * and variables.<br />
     * <b>Warning:</b> A CST generated from a string via {@link #parseCString(String)} is
     * not guaranteed to have the same string as a result from this method!
     * 
     * @param tree The tree to convert into a C condition string.
     * @return 
     * @throws ConstraintException
     */
    public static String toCString(ConstraintSyntaxTree tree) throws ConstraintException {
        return toCStringInternal(tree, true);
    }
    
    private static String toCStringInternal(ConstraintSyntaxTree tree, boolean first) throws ConstraintException {
        StringBuffer result = new StringBuffer();
        
        if (tree instanceof Variable) {
            result.append(((Variable) tree).getVariable().getName());
            
        } else if (tree instanceof OCLFeatureCall) {
            OCLFeatureCall call = (OCLFeatureCall) tree;
            
            
            if (call.getOperation().equals(OclKeyWords.AND)) {
                if (!first) {
                    result.append("(");
                }
                result.append(toCStringInternal(call.getOperand(), false));
                result.append(" && ");
                result.append(toCStringInternal(call.getParameter(0), false));
                if (!first) {
                    result.append(")");
                }
                
            } else if (call.getOperation().equals(OclKeyWords.OR)) {
                if (!first) {
                    result.append("(");
                }
                result.append(toCStringInternal(call.getOperand(), false));
                result.append(" || ");
                result.append(toCStringInternal(call.getParameter(0), false));
                if (!first) {
                    result.append(")");
                }
                
            } else if (call.getOperation().equals(OclKeyWords.NOT)) {
                result.append("!");
                result.append(toCStringInternal(call.getOperand(), false));
                
            } else {
                throw new ConstraintException("Invalid operation in CST: " + call.getOperation());
            }
            
        } else {
            throw new ConstraintException("Invalid element in CST: " + tree);
        }
        
        return result.toString();
    }
    
    /**
     * Filters duplicate formulas from the given list of formulas.<br/>
     * <b>TODO:</b> Filter method does not consider AST, it compares only the string representation.
     * @param formulas Formulas, which shall be filtered (may contain duplicates). Must not be <tt>null</tt>.
     * @return The filtered list of duplicate, won't be <tt>null</tt>.
     */
    public static List<String> filterDuplicates(List<String> formulas) {
        // Current equality check is only done on String.equals -> this could be improved
        Set<String> alreadyCollectedFormulas = new HashSet<>();
        List<String> results = new ArrayList<>();
        for (int i = 0; i < formulas.size(); i++) {
            String currentFormula = formulas.get(i);
            if (!alreadyCollectedFormulas.contains(currentFormula)) {
                alreadyCollectedFormulas.add(currentFormula);
                results.add(currentFormula);
            }
        }
        
        if (results.size() < formulas.size()) {
            int skipped = formulas.size() - results.size();
            System.out.println("Skipped " + skipped + " from " + formulas.size());
        }
        
        return results;
    }
    
    /**
     * Filters and merges the given constraints with an greedy algorithm
     * @param formulas Formulas which belongs together and maybe merged to one big constraint.
     * @param mergeOperation The operation how the both constraints shall be connected, e.g.,
     *     <tt>or</tt> in: <tt>(E and A) or (E and B)</tt>
     * @return The filtered and merged constraints.
     */
    public static List<String> reduceFormulas(List<String> formulas) {
        List<ConstraintSyntaxTree> mergedAndFiltered = ConstraintMerger.mergeConstraints(formulas, OclKeyWords.OR);
        List<String> results = new ArrayList<>();
        boolean exceptionCaught = false;
        
        for (int i = 0; i < mergedAndFiltered.size(); i++) {
            try {
                results.add(toCString(mergedAndFiltered.get(i)));
            } catch (ConstraintException e) {
                exceptionCaught = true;
                e.printStackTrace();
            }
        }
        
        return exceptionCaught ? formulas : results;
    }
        
}
