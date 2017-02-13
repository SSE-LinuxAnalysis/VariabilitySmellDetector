package de.uni_hildesheim.sse.smell.filter.input;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.kbuild.KbuildMiner;
import de.uni_hildesheim.sse.kernel_miner.util.ZipArchive;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;
import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.ConditionBlock;
import de.uni_hildesheim.sse.smell.data.ConditionType;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariablePresenceConditions;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.IFilter;

public class TypeChefArchiveReader implements IFilter {

    private ZipArchive archive;
    
    private File kbuildLocation;
    
    public TypeChefArchiveReader(File location, File kbuildLocation) {
        archive = new ZipArchive(location);
        this.kbuildLocation = kbuildLocation;
    }
    
    private void readSourceFileBlocks(SourceFile file, Map<String, VariablePresenceConditions> variablePresenceConditions) throws IOException {
        File csvFile = new File(file.getPath() + ".csv");
        
        if (!archive.containsFile(csvFile)) {
            System.out.println("Warning: file " + csvFile.toString() + " does not exist in archive");
            return;
        }
        
        TypeChefCsvReader csvReader = new TypeChefCsvReader(archive.getInputStream(csvFile), variablePresenceConditions, file);
        csvReader.read(false);
    }
    
    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws FilterException {
        
        progressPrinter.start("TypeChefArchiveReader Step 1", 0);
        
        List<SourceFile> sourceFiles;
        
        try {
            sourceFiles = KbuildMiner.readOutput(kbuildLocation);
        } catch (IOException e) {
            throw new FilterException(e);
        }
        
        progressPrinter.start("TypeChefArchiveReader Step 2", sourceFiles.size());
        
        Map<String, VariablePresenceConditions> variablePresenceConditions = new HashMap<>(20000);
        
        for (SourceFile file : sourceFiles) {
            try {
                readSourceFileBlocks(file, variablePresenceConditions);
            } catch (IOException e) {
                System.out.println("Exception while reading CSV file " + file.getPath());
                e.printStackTrace(System.out);
            }
            progressPrinter.finishedOne();
        }
        
        List<IDataElement> result = new ArrayList<IDataElement>(20000);
        
        for (VariablePresenceConditions entry : variablePresenceConditions.values()) {
            
            if (!entry.getVariable().startsWith("CONFIG_")) {
                System.out.println("Ignoring not CONFIG_ variable: " + entry.getVariable());
//                for (String pc : entry.getPresenceConditions()) {
//                    System.out.println("\t" + pc);
//                }
            } else {
                result.add(entry);
            }
            
        }
        
        Collections.sort(result, new Comparator<IDataElement>() {
            @Override
            public int compare(IDataElement o1, IDataElement o2) {
                VariablePresenceConditions e1 = (VariablePresenceConditions) o1;
                VariablePresenceConditions e2 = (VariablePresenceConditions) o2;
                return Integer.compare(e1.getNumPresenceConditions(), e2.getNumPresenceConditions());
            }
        });
        
        return result;
    }
    
    private static class TypeChefCsvReader extends AbstractCSVReader {

        private static final int PC_INDEX = 3;
        
        private Map<String, VariablePresenceConditions> variablePresenceConditions;
        
        private SourceFile file;
        
        public TypeChefCsvReader(InputStream in, Map<String, VariablePresenceConditions> variablePresenceConditions, SourceFile file) {
            super(in);
            this.variablePresenceConditions = variablePresenceConditions;
            this.file = file;
        }

        @Override
        public void readLine(String[] fields) {

            StringBuilder condition = new StringBuilder();
            
            if (file.getPresenceCondition() != null && !(file.getPresenceCondition() instanceof True)) {
                condition.append("(").append(file.getPresenceCondition().toString())
                        .append(") && (").append(fields[PC_INDEX]).append(")");
            } else {
                condition.append(fields[PC_INDEX]);
            }
            
            ConditionBlock block = new ConditionBlock(null, 0, 0, ConditionType.IF, 0, 0, null, condition.toString());
            
            Set<String> vars = block.getVariablesInNormalizedCondition();
            
            for (String variable : vars) {
                VariablePresenceConditions vpc = variablePresenceConditions.get(variable);
                if (vpc == null) {
                    vpc = new VariablePresenceConditions(variable);
                    variablePresenceConditions.put(variable, vpc);
                }
                
                vpc.addPresenceCondition(block.getNormalizedCondition());
            }
            
        }
        
    }

}
