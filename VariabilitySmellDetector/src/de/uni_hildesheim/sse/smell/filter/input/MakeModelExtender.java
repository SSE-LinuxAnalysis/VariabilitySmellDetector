package de.uni_hildesheim.sse.smell.filter.input;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.ConditionBlock;
import de.uni_hildesheim.sse.smell.data.ConditionType;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;
import de.uni_hildesheim.sse.smell.filter.old_permanent_parent.SmellDetector;

/**
 * A filter that extends a given presence conditions with extra
 * {@link ConditionBlock}s for presence conditions of the source files, imposed
 * by kbuild.
 * <br />
 * <b>Input</b>: {@link ConditionBlock}s<br />
 * <b>Output</b>: {@link ConditionBlock}s<br />
 * 
 * @author Adam Krafczyk
 * @author El-Sharkawy
 */
public class MakeModelExtender implements IFilter {
    
    private BufferedReader filePresenceIn;
    private boolean skipFirstLine;
    
    /**
     * Information from Make model in form of:
     * (File, Condition).
     */
    private Map<String, String> filePresenceConditions;
    
    public MakeModelExtender(String filePresenceConditionFile, boolean skipFirstLine) throws FileNotFoundException {
        filePresenceIn = new BufferedReader(new FileReader(filePresenceConditionFile));
        this.skipFirstLine = skipFirstLine;
    }

    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter)
            throws WrongFilterException {
        
        List<IDataElement> result = new ArrayList<>();
        filePresenceConditions = new HashMap<>();
        Set<String> consideredFiles = new HashSet<>();
        
        progressPrinter.start(this, data.size() + filePresenceConditions.size());     
        try {
            parseFile();
        } catch (IOException e) {
            e.printStackTrace();
            return data;
        }
        
        extendPresenceCondtions(data, progressPrinter, result, consideredFiles);
        addMakeConditions(progressPrinter, result, consideredFiles);
            
        return result;
    }

    /**
     * Adds the information from the make model for files which do not have a presence condition.
     */
    private void addMakeConditions(IProgressPrinter progressPrinter, List<IDataElement> result,
            Set<String> consideredFiles) {
        for (Map.Entry<String, String> makeEntry : filePresenceConditions.entrySet()) {
            String fileName = makeEntry.getKey();
            String condition = makeEntry.getValue();
            if (null != fileName && !consideredFiles.contains(fileName)
                && null != condition && !condition.isEmpty()) {
                
                // System.out.println("[Debug] Adding file that doesn't contain any other PCs: " + fileName);
                
                ConditionBlock newBlock = new ConditionBlock(fileName, 0, 99999,
                    ConditionType.IF, 0, 0, condition, condition);
                result.add(newBlock);
            }
            progressPrinter.finishedOne();
        }
    }

    /**
     * Extends the presence conditions, extracted from code files.
     */
    private void extendPresenceCondtions(List<IDataElement> data, IProgressPrinter progressPrinter,
            List<IDataElement> result, Set<String> consideredFiles) throws WrongFilterException {
        String previousFile = null;
        String currentCondition = "";
        boolean ignoreFile = false;
        
        for (int i = 0; i < data.size(); i++) {
            if (!(data.get(i) instanceof ConditionBlock)) {
                throw new WrongFilterException(SmellDetector.class, ConditionBlock.class, data.get(i));
            }
            ConditionBlock block = (ConditionBlock) data.get(i);
            String currentFile = block.getFilename();
            if (previousFile == null || !currentFile.equals(previousFile)) {
                currentCondition = filePresenceConditions.get(currentFile);
                if (currentCondition == null) {
//                    System.out.println("[Warning ]No Kbuild PC for: " + block.getFilename() + ", ignoring all PCs inside of it");
                    currentCondition = "";
                    ignoreFile = true;
                } else {
                    // System.out.println("[Debug] Expanding file with Kbuild PC: " + block.getFilename());
                    
                    ignoreFile = false;
                }
                if (!currentCondition.equals("")) {
                    ConditionBlock newBlock = new ConditionBlock(currentFile, 0, 99999,
                            ConditionType.IF, 0, 0, currentCondition, currentCondition);
                    result.add(newBlock);
                }
                
                previousFile = currentFile;
            }
            
            
            if (!ignoreFile) {
                consideredFiles.add(currentFile);
                ConditionBlock blockCopy;
                if (currentCondition.equals("")) {
                    blockCopy = block;
                } else {
                    blockCopy= new ConditionBlock(block.getFilename(),
                            block.getLineStart(), block.getLineEnd(), block.getType(),
                            block.getIndentation() + 1, block.getStartingIfLine(),
                            block.getCondition(), currentCondition + " && (" + block.getNormalizedCondition() + ")");
                }
                
                result.add(blockCopy);
            }
            progressPrinter.finishedOne();
        }
    }
    
    private void parseFile() throws IOException {
        String line;
        
        if (skipFirstLine) {
            filePresenceIn.readLine();
        }
        
        while ((line = filePresenceIn.readLine()) != null) {
            String[] parts = line.split(";");
            if (filePresenceConditions.containsKey(parts[0])) {
                String existing = filePresenceConditions.get(parts[0]);
                String found = (parts.length > 1) ? parts[1] : "";
                if (!found.equals(existing)) {
                    System.out.println(parts[0] + " was found twice, with different presence conditions: "
                            + "\"" + existing + "\" and \"" + found + "\"");
                    System.out.println("Using the first one");
                }
            }
            if (parts.length == 1 || parts[1].trim().equals("")) {
                filePresenceConditions.put(parts[0], "");
            } else {
                filePresenceConditions.put(parts[0], parts[1]);
            }
        }
    }

}
