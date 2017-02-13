package de.uni_hildesheim.sse.smell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.kbuild.KbuildMiner;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;
import de.uni_hildesheim.sse.kernel_miner.util.ZipArchive;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariablePresenceConditions;
import de.uni_hildesheim.sse.smell.data.VariableWithSolutions;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.input.ConditionBlockReader;
import de.uni_hildesheim.sse.smell.filter.input.ConfigVarFilter;
import de.uni_hildesheim.sse.smell.filter.input.MakeModelExtender;
import de.uni_hildesheim.sse.smell.filter.input.TypeChefArchiveReader;
import de.uni_hildesheim.sse.smell.filter.input.VariableWithSolutionsReader;
import de.uni_hildesheim.sse.smell.filter.kaestraints.NoDominatingFilter;
import de.uni_hildesheim.sse.smell.filter.kaestraints.PcSmellDetector;
import de.uni_hildesheim.sse.smell.filter.kaestraints.PresenceConditionFinder;
import de.uni_hildesheim.sse.smell.filter.old_permanent_parent.AbsoluteNestedDependsFinder;
import de.uni_hildesheim.sse.smell.filter.old_permanent_parent.InconsistentParentFilter;
import de.uni_hildesheim.sse.smell.filter.old_permanent_parent.NestedDependsFinder;
import de.uni_hildesheim.sse.smell.filter.old_permanent_parent.PaperSmellDetector;
import de.uni_hildesheim.sse.smell.filter.old_permanent_parent.SmellDetector;
import de.uni_hildesheim.sse.smell.filter.output.CsvDumper;
import de.uni_hildesheim.sse.smell.filter.output.CsvPrinter;
import de.uni_hildesheim.sse.smell.filter.util.DataSizePrinter;
import de.uni_hildesheim.sse.smell.filter.util.RedundantSolutionFilter;
import de.uni_hildesheim.sse.smell.filter.util.KaestraintSolutionAnnotator;
import de.uni_hildesheim.sse.smell.filter.util.VisibleVariableFilter;

@SuppressWarnings("unused")
public class Main {

    private static final String INPUT_FOLDER = "input/";
    private static final String OUTPUT_FOLDER = "output/";

    // Versions
    private static final String BUSYBOX_V_1_24_2 = "1.24.2"; // has no makemodel
    private static final String LINUX_2_6_33 = "linux-2.6.33.3"; // x86 only
    private static final String LINUX_4_2 = "linux-4.2"; // x86 only
    private static final String LINUX_4_4 = "linux-4.4"; // has no makemodel, x86 only
    private static final String LINUX_4_4_1 = "linux-4.4.1"; // x86 & nios2
    private static final String LINUX_4_4_1_PAPER = "linux-4.4.1_paper"; // all archs
    private static final String LINUX_4_5_2 = "linux-4.5.2"; // x86 only

    // Architectures
    private static final String BUSYBOX_ARCH = "busybox"; // busybox does not have different archs
    private static final String[] LINUX_ARCHS = {
            // Commented-out values are not parsable by KconfigReader.
            "alpha",
            //"arm",
            "arc",
            "arm64",
            //"avr32",
            //"blackfin",
            //"c6x", 
            "cris",
            //"frv",
            "h8300",
            "hexagon",
            "ia64",
            "m32r",
            //"m68k",
            "metag",
            "microblaze",
            "mips",
            "mn10300",
            "nios2",
            //"openrisc",
            "parisc",
            "powerpc",
            //"s390",
            //"score",
            "sh",
            "sparc",
            "tile",
            //"um",
            //"unicore32",
            "x86",
            "xtensa"
    };

    public static void main(String[] args) throws Exception {
//        paperRunWitouthMakemodel(BUSYBOX_V_1_24_2, BUSYBOX_ARCH); // TODO need to be fixed first!
//        paperRunWithMakemodel(LINUX_4_4_1, "nios2");
//        paperRunWithMakemodel(LINUX_4_4_1, "x86");
        
//        kaestraintsRun(LINUX_4_4_1, "x86");
        kaestraintResultPresentation(LINUX_4_4_1, "x86", "E:/research/linux_versions/linux-4.4.1");
        
//        variableFinderRun(OUTPUT_FOLDER + "linux-4.4.1/x86.kaestraints.result.csv",
//                "C:/localUserFiles/krafczyk/research/linux_versions/linux-4.4.1",
//                OUTPUT_FOLDER + "locations.test.csv");
//        tmp();
//        esxiTmp();
//        esciTmp2();
    }

    private static void smellDetectorV1WithMakeModel(String version, String arch) throws Exception {
        String structure = INPUT_FOLDER + version + "/structure.csv";
        String makemodel = INPUT_FOLDER + version + "/" + arch + ".makemodel.csv";
        String kconfig = INPUT_FOLDER + version + "/" + arch + ".dimacs";
        
        String missingVars = OUTPUT_FOLDER + version + "/" + arch + ".missing_vars.csv";
        String makemodelExtended = OUTPUT_FOLDER + version + "/" + arch + ".makemodel_extended_structure.csv";
        String candidates = OUTPUT_FOLDER + version + "/" + arch + ".nested_depends_candidates.csv";
        String result = OUTPUT_FOLDER + version + "/" + arch + ".smelldetectorv1.result.csv";
        
        Pipeline pipeline = new Pipeline(new StreamProgressPrinter());
        
        pipeline.addFilter(new ConditionBlockReader(structure, true));
        pipeline.addFilter(new ConfigVarFilter(true, false, false));

        pipeline.addFilter(new MakeModelExtender(makemodel, false));
        pipeline.addFilter(new CsvPrinter(makemodelExtended));

        pipeline.addFilter(new NestedDependsFinder());
        pipeline.addFilter(new CsvPrinter(candidates));

        pipeline.addFilter(new AbsoluteNestedDependsFinder(candidates, structure, makemodelExtended, true));
        pipeline.addFilter(new SmellDetector(kconfig, missingVars));
        pipeline.addFilter(new CsvPrinter(result));

        System.out.println("Starting to run for " + version + ", arch " + arch);
        pipeline.run();
    }

    private static void paperRunWithMakemodel(String version, String arch) throws Exception {
        String structure = INPUT_FOLDER + version + "/structure.csv";
        String makemodel = INPUT_FOLDER + version + "/" + arch + ".makemodel.csv";
        String kconfig = INPUT_FOLDER + version + "/" + arch + ".dimacs";
        
//        String missingVars = OUTPUT_FOLDER + version + "/" + arch + ".missing_vars.csv";
        String makemodelExtended = OUTPUT_FOLDER + version + "/" + arch + ".makemodel_extended_structure.csv";
        String candidates = OUTPUT_FOLDER + version + "/" + arch + ".nested_depends_candidates.csv";
        String result = OUTPUT_FOLDER + version + "/" + arch + ".paper.result.csv";
        
        Pipeline pipeline = new Pipeline(new StreamProgressPrinter());

        pipeline.addFilter(new ConditionBlockReader(structure, true));
        pipeline.addFilter(new ConfigVarFilter(true, false, false));

        pipeline.addFilter(new MakeModelExtender(makemodel, false));
        pipeline.addFilter(new CsvPrinter(makemodelExtended));

        pipeline.addFilter(new NestedDependsFinder());
        pipeline.addFilter(new CsvPrinter(candidates));

        pipeline.addFilter(new AbsoluteNestedDependsFinder(candidates, structure, makemodelExtended, true));
        pipeline.addFilter(new InconsistentParentFilter());
        pipeline.addFilter(new PaperSmellDetector(kconfig, null));
        pipeline.addFilter(new CsvPrinter(result));

        System.out.println("Starting to run for " + version + ", arch " + arch);
        pipeline.run();
    }
    
    private static void paperRunWithoutMakemodel(String version, String arch) throws Exception {
        String structure = INPUT_FOLDER + version + "/structure.csv";
//        String makemodel = INPUT_FOLDER + version + "/" + arch + ".makemodel.csv";
        String kconfig = INPUT_FOLDER + version + "/" + arch + ".dimacs";
        
//        String missingVars = OUTPUT_FOLDER + version + "/" + arch + ".missing_vars.csv";
        String onlyConfigs = OUTPUT_FOLDER + version + "/" + arch + ".only_configs.csv";
        String candidates = OUTPUT_FOLDER + version + "/" + arch + ".nested_depends_candidates.csv";
        String result = OUTPUT_FOLDER + version + "/" + arch + ".paper.result.csv";
        
        Pipeline pipeline = new Pipeline(new StreamProgressPrinter());
        
        pipeline.addFilter(new ConditionBlockReader(structure, true));
        pipeline.addFilter(new ConfigVarFilter(true, false, false));
        
        pipeline.addFilter(new CsvPrinter(onlyConfigs));
        
        pipeline.addFilter(new NestedDependsFinder());
        pipeline.addFilter(new CsvPrinter(candidates));
        
        pipeline.addFilter(new AbsoluteNestedDependsFinder(candidates, structure, onlyConfigs, true));
        pipeline.addFilter(new PaperSmellDetector(kconfig, null));
        pipeline.addFilter(new CsvPrinter(result));

        System.out.println("Starting to run for " + version + ", arch " + arch);
        pipeline.run();
    }
    
    private static void kaestraintsRun(String version, String arch) throws Exception {
        String structure = INPUT_FOLDER + version + "/structure.csv";
        String makemodel = INPUT_FOLDER + version + "/" + arch + ".makemodel.csv";
        String kconfig = INPUT_FOLDER + version + "/" + arch + ".dimacs";
        
        String pcs = OUTPUT_FOLDER + version + "/" + arch + ".pcs.csv";
        String result = OUTPUT_FOLDER + version + "/" + arch + ".kaestraints.result.csv";
        
        Pipeline pipeline = new Pipeline(new StreamProgressPrinter());
        
        pipeline.addFilter(new ConditionBlockReader(structure, true));
        pipeline.addFilter(new MakeModelExtender(makemodel, false));
        
        /* 
         * PresenceConditionFinder: Creates list in form of:
         * A -> List of PCs
         */
        pipeline.addFilter(new PresenceConditionFinder());
        
        pipeline.addFilter(new CsvPrinter(pcs));
        
        /* 
         * NoDominatingFilter: Small filter, checks whether a variable
         * has non dependent PC, which can be toggled directly by this variable
         * in any configuration (or more precisely has no "Feature Effect")
         */
        pipeline.addFilter(new NoDominatingFilter());
        pipeline.addFilter(new PcSmellDetector(kconfig, result, 32));
        
        // no output needed, because PcSmellDetector already writes it as it finds them
//        pipeline.addFilter(new CsvPrinter(result));
        
        System.out.println("Starting to run for " + version + ", arch " + arch);
        pipeline.run();
    }
    
    /**
     * Improves the result of a kaestraintsRun() by adding source locations, prompts, etc. 
     */
    private static void kaestraintResultPresentation(String version, String arch, String linuxTree) throws Exception {
        String rsfFile = INPUT_FOLDER + version + "/" + arch + ".rsf";
        
        String kaestraintResult = OUTPUT_FOLDER + version + "/" + arch + ".kaestraints.result.csv";
        String result = OUTPUT_FOLDER + version + "/" + arch + ".kaestraints.analysis_template.csv";
        
        Pipeline pipeline = new Pipeline(new StreamProgressPrinter());
        
        pipeline.addFilter(new VariableWithSolutionsReader(kaestraintResult, true));
        pipeline.addFilter(new RedundantSolutionFilter());
        pipeline.addFilter(new KaestraintSolutionAnnotator(linuxTree, rsfFile));
        pipeline.addFilter(new CsvPrinter(result));
        
        pipeline.run();
    }
    
    private static void tmp() throws Exception {
        Logger.init();
        
        Pipeline p = new Pipeline(new StreamProgressPrinter());
        
        p.addFilter(new TypeChefArchiveReader(
                new File("C:/localUserFiles/krafczyk/research/typechef/typechef_output_linux-4.4.zip"),
                new File("C:/localUserFiles/krafczyk/tmp/typechef_windows/kbuild_pcs/x86.pcs.txt")));
        
        p.addFilter(new CsvPrinter(OUTPUT_FOLDER + "/typechef_linux-4.4.pcs.csv"));
        
        p.addFilter(new NoDominatingFilter());
        p.addFilter(new PcSmellDetector(INPUT_FOLDER + "linux-4.4/x86.dimacs", 1));
        
        p.addFilter(new CsvPrinter(OUTPUT_FOLDER + "/typechef_linux-4.4.result.csv"));
        
        p.run();
    }
    
    private static void esxiTmp() throws Exception {
        Logger.init();
        
        Pipeline p = new Pipeline(new StreamProgressPrinter());
        
        p.addFilter(new TypeChefArchiveReader(
                new File("/home/adam/typechef_tests/tools/kernelminer/typechef_output_linux-4.4_test.zip"),
                new File("/home/adam/typechef_tests/kbuild_pcs/linux-4.4/x86.pcs.txt")));
        
        
        ZipArchive archive = new ZipArchive(new File("result_typechef_linux-4.4.zip"));
        p.addFilter(new CsvDumper(new PrintStream(archive.getOutputStream(new File("pcs.csv")))));
        
//        p.addFilter(new CsvPrinter("typechef_linux-4.4.pcs.csv"));
        
//        p.addFilter(new NoDominatingFilter());
//        p.addFilter(new PcSmellDetector("x86.dimacs"));
//        
//        p.addFilter(new CsvPrinter("typechef_linux-4.4.result.csv"));
        
        p.run();
    }
    
    private static void esciTmp2() throws Exception {
        Logger.init();
        
        ZipArchive archive = new ZipArchive(new File("result_typechef_linux-4.4.zip"));
        BufferedReader in = new BufferedReader(new InputStreamReader(archive.getInputStream(new File("pcs.csv"))));
        PrintStream out = new PrintStream(new FileOutputStream("typechef_linux-4.4.result.csv"));
        
        out.println(new VariableWithSolutions("").headertoCsvLine());
        
        // skip first line
        in.readLine();
        
        String line;
        while ((line = in.readLine()) != null) {
            String[] parts = line.split(";");
            line = null;
            
            final VariablePresenceConditions element = new VariablePresenceConditions(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                element.addPresenceCondition(parts[i]);
            }
            
            System.out.println("Running for " + element.getVariable() + " with " + element.getNumPresenceConditions() + " PCs");
            
            Pipeline pipeline = new Pipeline(new StreamProgressPrinter());
            pipeline.addFilter(new IFilter() {
                @Override
                public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws FilterException {
                    ArrayList<IDataElement> result = new ArrayList<>(1);
                    result.add(element);
                    return result;
                }
            });
            pipeline.addFilter(new NoDominatingFilter());
            pipeline.addFilter(new PcSmellDetector("x86.dimacs", 1));
            pipeline.addFilter(new DataSizePrinter());
            pipeline.addFilter(new CsvPrinter(out, false, false));
            
            pipeline.run();
        }
        
        out.close();
        in.close();
    }
    
}
