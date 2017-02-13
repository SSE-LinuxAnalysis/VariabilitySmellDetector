package de.uni_hildesheim.sse.smell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Conjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Disjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.False;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;
import de.uni_hildesheim.sse.kernel_miner.util.parser.CStyleBooleanGrammar;
import de.uni_hildesheim.sse.kernel_miner.util.parser.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_miner.util.parser.Parser;
import de.uni_hildesheim.sse.kernel_miner.util.parser.VariableCache;

public class MakexKbuildMinerComparator {

    private static final String INPUT_FOLDER = "input/";
    
    private static final File LINUX_TREE = new File("E:/research/linux_versions/linux-4.4.1");

    private static final VariableCache CACHE = new VariableCache();
    private static final Parser<Formula> PARSER = new Parser<>(new CStyleBooleanGrammar(CACHE) {
        
        public boolean isIdentifierChar(char[] str, int i) {
            return super.isIdentifierChar(str, i) || str[i] == '-';
        };
        
    });
    
    private static Map<String, Formula> readFile(String file) throws IOException, ExpressionFormatException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        
        Map<String, Formula> result = new HashMap<>();
        
        String line;
        while ((line = in.readLine()) != null) {
            String[] parts = line.split(";");
            
            if (parts.length == 1 || parts[1].equals("")) {
                result.put(parts[0], new True());
            } else {
                try {
                    result.put(parts[0], PARSER.parse(parts[1]));
                } catch (ExpressionFormatException e) {
                    System.err.println(parts[1]);
                    throw e;
                }
            }
        }
        
        in.close();
        
        return result;
    }
    
    private static void getVariables(Formula f, Set<String> variables) {
        if (f instanceof Variable) {
            variables.add(((Variable) f).getName());
        } else if (f instanceof True) {
        } else if (f instanceof False) {
        } else if (f instanceof Negation) {
            getVariables(((Negation) f).getFormula(), variables);
        } else if (f instanceof Conjunction) {
            getVariables(((Conjunction) f).getLeft(), variables);
            getVariables(((Conjunction) f).getRight(), variables);
        } else if (f instanceof Disjunction) {
            getVariables(((Disjunction) f).getLeft(), variables);
            getVariables(((Disjunction) f).getRight(), variables);
        } else {
            throw new RuntimeException("Ahhh!");
        }
    }
    
    private static boolean containsSameVariables(Formula f1, Formula f2) {
        Set<String> v1 = new HashSet<>();
        getVariables(f1, v1);
        Set<String> v2 = new HashSet<>();
        getVariables(f2, v2);
        
        for (String s : v1) {
            if (!v2.contains(s)) {
                return false;
            }
        }
        for (String s : v2) {
            if (!v1.contains(s)) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean foundInLinuxTree(String filename) {
        filename = filename.substring(1);
        
        File toCheck = new File(LINUX_TREE, filename);
        
        return toCheck.isFile();
    }
    
    public static void main(String[] args) throws Exception {
        final String version = "linux-4.4.1";
        final String arch = "x86";

        String kbuildMinerFile = INPUT_FOLDER + version + "/" + arch + ".makemodel.csv";
        String makexFile = INPUT_FOLDER + version + "/" + arch + ".makemodel_makex.csv";
        
        Map<String, Formula> kbuildMiner = readFile(kbuildMinerFile);
        Map<String, Formula> makex = readFile(makexFile);
        
        int same = 0;
        int sameVars = 0;
        
        int makexNotFound = 0;
        int kbuildMinerNotFound = 0;
        
        int onlyMakex = 0;
        int onlyMakexAndNotFound = 0;
        int onlyKbuildMiner = 0;
        int onlyKbuildMinerAndNotFound = 0;
        
        for (Map.Entry<String, Formula> makexE : makex.entrySet()) {
            Formula kbuildMinerE = kbuildMiner.get(makexE.getKey());
            if (kbuildMinerE != null) {
                if (kbuildMinerE.equals(makexE.getValue())) {
                    same++;
                }
                
                if (containsSameVariables(kbuildMinerE, makexE.getValue())) {
                    sameVars++;
                }
            } else {
                onlyMakex++;
                if (!foundInLinuxTree(makexE.getKey())) {
                    onlyMakexAndNotFound++;
                }
            }
            
            if (!foundInLinuxTree(makexE.getKey())) {
                makexNotFound++;
            }
        }
        for (Map.Entry<String, Formula> kbuilcMinerE : kbuildMiner.entrySet()) {
            Formula makexE = makex.get(kbuilcMinerE.getKey());
            if (makexE == null) {
                onlyKbuildMiner++;
                if (!foundInLinuxTree(kbuilcMinerE.getKey())) {
                    onlyKbuildMinerAndNotFound++;
                }
            }
            
            if (!foundInLinuxTree(kbuilcMinerE.getKey())) {
                kbuildMinerNotFound++;
            }
        }
        
        System.out.println();
        System.out.println("=== Summary ===");
        System.out.println("KbuildMiner has " + kbuildMiner.size() + " entries");
        System.out.println("Makex has " + makex.size() + " entries");
        System.out.println("Makex has " + onlyMakex + " entries that KbuildMiner does not have, " + onlyMakexAndNotFound + " of which are not found in a Linux tree");
        System.out.println("KbuildMiner has " + onlyKbuildMiner + " entries that Makex does not have, " + onlyKbuildMinerAndNotFound + " of which are not found in a Linux tree");
        System.out.println("Makex has " + makexNotFound + " entries that are not found in the Linux tree");
        System.out.println("KbuildMiner has " + kbuildMinerNotFound + " entries that are not found in the Linux tree");
        System.out.println(same + " entries are the same");
        System.out.println(sameVars + " have the same variables");
    }
    
}
