package de.uni_hildesheim.sse.smell;

import de.uni_hildesheim.sse.smell.filter.input.ConditionBlockReader;
import de.uni_hildesheim.sse.smell.filter.input.ConfigVarFilter;
import de.uni_hildesheim.sse.smell.filter.input.MakeModelExtender;
import de.uni_hildesheim.sse.smell.filter.input.VariableWithSolutionsReader;
import de.uni_hildesheim.sse.smell.filter.kaestraints.NoDominatingFilter;
import de.uni_hildesheim.sse.smell.filter.kaestraints.PcSmellDetector;
import de.uni_hildesheim.sse.smell.filter.kaestraints.PresenceConditionFinder;
import de.uni_hildesheim.sse.smell.filter.old_permanent_parent.AbsoluteNestedDependsFinder;
import de.uni_hildesheim.sse.smell.filter.old_permanent_parent.InconsistentParentFilter;
import de.uni_hildesheim.sse.smell.filter.old_permanent_parent.NestedDependsFinder;
import de.uni_hildesheim.sse.smell.filter.old_permanent_parent.PaperSmellDetector;
import de.uni_hildesheim.sse.smell.filter.old_permanent_parent.SmellDetector;
import de.uni_hildesheim.sse.smell.filter.output.CsvPrinter;
import de.uni_hildesheim.sse.smell.filter.util.RedundantSolutionFilter;
import de.uni_hildesheim.sse.smell.filter.util.VariableLocationsFinder;
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
        paperRunWithMakemodel(LINUX_4_4_1, "x86");
//        kaestraintsRun(LINUX_4_4_1, "x86");
//        variableFinderRun(OUTPUT_FOLDER + "linux-4.4.1/x86.kaestraints.result.csv",
//                "C:/localUserFiles/krafczyk/research/linux_versions/linux-4.4.1",
//                OUTPUT_FOLDER + "locations.test.csv");
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
        pipeline.addFilter(new PcSmellDetector(kconfig));
        
        pipeline.addFilter(new CsvPrinter(result));
        
        System.out.println("Starting to run for " + version + ", arch " + arch);
        pipeline.run();
    }
    
    private static void splitKaestraintResultsByPrompt(String version, String arch) throws Exception {
        String rsf = INPUT_FOLDER + version + "/" + arch + ".rsf";
        
        String result = OUTPUT_FOLDER + version + "/" + arch + ".kaestraints.result.csv";
        String withPrompt = OUTPUT_FOLDER + version + "/" + arch + ".kaestraints.result.with_prompt.csv";
        String withoutPrompt = OUTPUT_FOLDER + version + "/" + arch + ".kaestraints.result.without_prompt.csv";
        
        Pipeline p1 = new Pipeline(new StreamProgressPrinter());
        p1.addFilter(new VariableWithSolutionsReader(result, true));
        p1.addFilter(new RedundantSolutionFilter());
        p1.addFilter(new VisibleVariableFilter(rsf, true));
        p1.addFilter(new CsvPrinter(withPrompt));
        
        Pipeline p2 = new Pipeline(new StreamProgressPrinter());
        p2.addFilter(new VariableWithSolutionsReader(result, true));
        p2.addFilter(new RedundantSolutionFilter());
        p2.addFilter(new VisibleVariableFilter(rsf, false));
        p2.addFilter(new CsvPrinter(withoutPrompt));
        
        System.out.println("Starting to run for " + version + ", arch " + arch);
        p1.run();
        p2.run();
    }
    
    private static void variableFinderRun(String pcFile, String linuxTree, String resultFile) throws Exception {
        Pipeline pipeline = new Pipeline(new StreamProgressPrinter());
        
        pipeline.addFilter(new VariableWithSolutionsReader(pcFile, true));
        pipeline.addFilter(new VariableLocationsFinder(linuxTree));
        pipeline.addFilter(new CsvPrinter(resultFile));
        
        pipeline.run();
    }
    
}
