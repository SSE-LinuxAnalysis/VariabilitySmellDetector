package de.uni_hildesheim.sse.smell;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.kbuild.KbuildMiner;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Conjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Disjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;

public class KbuildMinerToCsvConverter {

    private static final String INPUT_FOLDER = "input/";
    
    private static void convertKbuildMinerOutputToCsv(String version, String arch) throws Exception {
        String inFile = INPUT_FOLDER + version + "/" + arch + ".makemodel.txt";
        String outFile = INPUT_FOLDER + version + "/" + arch + ".makemodel.csv";
        String dimacsModel = INPUT_FOLDER + version + "/" + arch + ".dimacs";
        
        Logger.init();
        List<SourceFile> files = KbuildMiner.readOutput(new File(inFile));
        
        Set<String> modules = CreateModulesTxt.getModules(dimacsModel);
        
        PrintWriter out = new PrintWriter(outFile);
        for (SourceFile sc : files) {
            Formula pc = sc.getPresenceCondition();
            String pcStr = "";
            if (pc != null && !(pc instanceof True)) {
                pc = removeNonTristateModules(pc, modules);
                pcStr = pc.toString();
            }
            
            String path = sc.getPath().getPath();
            path = path.replace('\\', '/');
            path = "./" + path;
            
            out.println(path + ";" + pcStr);
        }
        
        out.close();
    }
    
    private static Formula removeNonTristateModules(Formula in, Set<String> modules) {
        
        // TODO: heuristic: only on right side of disjunctions
        
        if (in instanceof Disjunction) {
            Disjunction dis = (Disjunction) in;
            
            if (dis.getRight() instanceof Variable) {
                Variable var = (Variable) dis.getRight();
                if (!modules.contains(var.getName())) {
//                    System.out.println("Dropping non-tristate module " + var.getName());
//                    System.out.println("  " + dis + " -> " + dis.getLeft());
                    return dis.getLeft();
                }
            }
            
            return new Disjunction(removeNonTristateModules(dis.getLeft(), modules),
                    removeNonTristateModules(dis.getRight(), modules));
            
        } else if (in instanceof Conjunction) {
            Conjunction con = (Conjunction) in;
            return new Conjunction(removeNonTristateModules(con.getLeft(), modules),
                    removeNonTristateModules(con.getRight(), modules));
            
        } else if (in instanceof Negation) {
            Negation neg = (Negation) in;
            return new Negation(removeNonTristateModules(neg.getFormula(), modules));
            
        }
        
        return in;
        
    }
    
    public static void main(String[] args) throws Exception {
        convertKbuildMinerOutputToCsv("linux-4.4.1", "x86");
    }
    
}
