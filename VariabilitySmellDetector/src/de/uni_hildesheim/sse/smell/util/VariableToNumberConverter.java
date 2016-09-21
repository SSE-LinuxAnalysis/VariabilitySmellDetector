package de.uni_hildesheim.sse.smell.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;

/**
 * A converter to convert variables names into numbers of a given DIMACS model,
 * and vice-versa.
 * 
 * @author Adam Krafczyk
 */
public class VariableToNumberConverter {
    
    private HashMap<String, Integer> mapping;
    
    private int maxNumber = 0;
    
    /**
     * Creates a {@link VariableToNumberConverter} for the given DIMACS model.
     * This expects the mapping to be at the top of the DIMACS file in the format:
     * <br /><code>c &lt;NUMBER&gt; &lt;VARIABLE_NAME&gt;</code>
     * <br />(undertaker format)
     * 
     * @param dimacsModelPath The path to the DIMACS model.
     * @param prefix A prefix that is added before all variables found in the DIMACS model file.
     * 
     * @throws IOException If reading the file fails.
     */
    public VariableToNumberConverter(String dimacsModelPath, String prefix) throws IOException {
        this();
        read(dimacsModelPath, prefix);
    }
    
    /**
     * Creates an empty {@link VariableToNumberConverter}.
     */
    public VariableToNumberConverter() {
        mapping = new HashMap<>();
    }
    
    /**
     * Parses the given DIMACS file and fills the internal mapping.
     * This expects the mapping to be at the top of the DIMACS file in the format:
     * <br /><code>c &lt;NUMBER&gt; &lt;VARIABLE_NAME&gt;</code>
     * <br />(undertaker format)
     * 
     * @param dimacsModelPath The file to parse.
     * @throws IOException If reading the file fails.
     */
    private void read(String dimacsModelPath, String prefix) throws IOException {
        BufferedReader file = new BufferedReader(new FileReader(dimacsModelPath));
        
        Pattern pattern = Pattern.compile("^c (\\d+) (.+)$");
        
        String line = null;
        while ((line = file.readLine()) != null) {
            if (!line.startsWith("c")) {
                break;
            }
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                int number = Integer.parseInt(matcher.group(1));
                if (number > maxNumber) {
                    maxNumber = number;
                }
                mapping.put(prefix + matcher.group(2), number);
            } 
        }
        
        file.close();
    }
    
    /**
     * @param name The name of the variable to search.
     * @return The number of the variable in the DIMACS model.
     * @throws VarNotFoundException If the variable was not found in the DIMACS model.
     */
    public int getNumber(String name) throws VarNotFoundException {
        try {
            return mapping.get(name);
        } catch (NullPointerException e) {
            throw new VarNotFoundException(name);
        }
    }
    
    /**
     * @param number The number of the variable in the DIMACS model.
     * @return The name of the variable, or <code>null</code> if not found.
     */
    public String getName(int number) {
        for (String name : mapping.keySet()) {
            // TODO: ENABLE_ for busybox
            try {
                if (getNumber(name) == number) {
                    return name;
                }
            } catch (VarNotFoundException e) {
                // can't happen
            }
        }
        return null;
    }
    
    
    /**
     * Adds a new variable <-> number mapping. Assures, that a variable with the
     * same name always gets the same number (i.e. it first checks, whether the
     * variable is already present).
     * 
     * @param name The name of the new variable.
     * @return The number for the new variable.
     */
    public int addVarible(String name) {
        if (mapping.get(name) != null) {
            return mapping.get(name);
        } else {
            mapping.put(name, ++maxNumber);
            return maxNumber;
        }
    }
    
    /**
     * @param a The left part.
     * @param b The right part.
     * @return The two parts merged together.
     */
    private static int[] merge(int[] a, int[] b) {
        int[] combined = new int[a.length + b.length];
        System.arraycopy(a, 0, combined, 0, a.length);
        System.arraycopy(b, 0, combined, a.length, b.length);
        return combined;
    }

    /**
     * Converts the given {@link ConstraintSyntaxTree} to a DIMACS line.
     * The {@link ConstraintSyntaxTree} must be in CNF format.
     * 
     * @param disjunctionTerm The {@link ConstraintSyntaxTree} to convert.
     * @return The cst in DIMACS format.
     * @throws VarNotFoundException If variables in the cst are not found in the DIMACS model.
     * @throws RuntimeException If the cst is not in CNF.
     * @throws ConstraintException If cst is not in CNF format (i.e. it contains anything but OR, NOT and variables).
     */
    public int[] convertToDimacs(ConstraintSyntaxTree disjunctionTerm) throws VarNotFoundException, ConstraintException {
        if (disjunctionTerm instanceof OCLFeatureCall) {
            OCLFeatureCall call = (OCLFeatureCall) disjunctionTerm;
            switch (call.getOperation()) {
            case OclKeyWords.NOT: {
                Variable var = (Variable) call.getOperand();
                String name = var.getVariable().getName();
                return new int[] { -1 * getNumber(name) };
            }
            case OclKeyWords.OR: {
                int[] left = convertToDimacs(call.getOperand());
                int[] right = convertToDimacs(call.getParameter(0));
                return merge(left, right);
            }
            default:
                throw new ConstraintException("Invalid operator: " + call.getOperation());
            }
        } else if (disjunctionTerm instanceof Variable) {
            Variable var = (Variable) disjunctionTerm;
            String name = var.getVariable().getName();
            return new int[] { getNumber(name) };
        }
        throw new ConstraintException("Invalid cst: " + disjunctionTerm);
    }
    
}
