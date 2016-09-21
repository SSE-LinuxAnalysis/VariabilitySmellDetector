package de.uni_hildesheim.sse.smell.filter.old_permanent_parent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.NestedDependsSmellCandidate;
import de.uni_hildesheim.sse.smell.data.PermanentNestedSmellCandidate;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;
import de.uni_hildesheim.sse.smell.filter.input.AbstractCSVReader;
import de.uni_hildesheim.sse.smell.util.ConditionUtils;
import de.uni_hildesheim.sse.smell.util.NestedVariablesContainer;

public class AbsoluteNestedDependsFinder implements IFilter {
    
    // Rows in NestedStructure
    private static final int NESTED_CONDITION_ROW = 2;
    private static final int OUTER_CONDITION_ROW = 3;
    
    // Rows in CompleteStructure
    private static final int LINE_START_ROW = 1;
    private static final int LINE_END_ROW = 2;
    private static final int INDENTATION_ROW = 4;
    private static final int NORMALIZED_CONDITION_ROW = 7;
    
    
    private NestedVariablesContainer permanentNestings;
    
    private String pathToNestedStructure;
    private String pathToCompleteStructure;
    private String pathToMakefile;
    private boolean skipFirstLine;
    
    public AbsoluteNestedDependsFinder(String pathToNestedStructure, String pathToCompleteStructure,
        boolean skipFirstLine) throws Exception {

        this(pathToNestedStructure, pathToCompleteStructure, null, skipFirstLine);
    }
    
    public AbsoluteNestedDependsFinder(String pathToNestedStructure, String pathToCompleteStructure,
        String pathToMakefile, boolean skipFirstLine) throws Exception {
        
        this.pathToNestedStructure = pathToNestedStructure;
        this.pathToCompleteStructure = pathToCompleteStructure;
        this.pathToMakefile = pathToMakefile;
        this.skipFirstLine = skipFirstLine;
        permanentNestings = new NestedVariablesContainer();
    }

    /**
     * Initializes the {@link NestedVariablesContainer} when this filter starts to work (needed as some input files do
     * not exist during the constructor call of this class). 
     * @param progressPrinter
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void init(IProgressPrinter progressPrinter) throws FileNotFoundException, IOException {
        // Handle nested elements
        AbstractCSVReader reader = new AbstractCSVReader(pathToNestedStructure) {
            
            @Override
            public void readLine(String[] fields) {
                Set<String> nestedVariables = ConditionUtils.getVariables(fields[NESTED_CONDITION_ROW]);
                Set<String> parentVariables = ConditionUtils.getVariables(fields[OUTER_CONDITION_ROW]);
                for (String nestedVar : nestedVariables) {
                    permanentNestings.addNesting(nestedVar, parentVariables);
                }
            }
        };
        reader.read(skipFirstLine);
        progressPrinter.finishedOne();
        
        // Remove all top level elements
        reader = new AbstractCSVReader(pathToCompleteStructure) {
            
            @Override
            public void readLine(String[] fields) {
                if ("0".equals(fields[INDENTATION_ROW])) {
                    Set<String> toplevelVariables = ConditionUtils.getVariables(fields[NORMALIZED_CONDITION_ROW]);
                    for (String toplevelVar : toplevelVariables) {
                        permanentNestings.clearParents(toplevelVar);
                    }
                    
                }
            }
        };
        reader.read(skipFirstLine);
        progressPrinter.finishedOne();
        
        // Consider toplevel variables from makefile
        if (null != pathToMakefile) {
            reader = new AbstractCSVReader(pathToMakefile) {
                
                @Override
                public void readLine(String[] fields) {
                    if ("0".equals(fields[INDENTATION_ROW]) && "0".equals(fields[LINE_START_ROW]) && "99999".equals(fields[LINE_END_ROW])) {
                        Set<String> toplevelVariables = ConditionUtils.getVariables(fields[NORMALIZED_CONDITION_ROW]);
                        for (String toplevelVar : toplevelVariables) {
                            permanentNestings.clearParents(toplevelVar);
                        }
                        
                    }
                }
            };
            reader.read(skipFirstLine);
        }
        progressPrinter.finishedOne();
    }
    
    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter)
        throws FilterException {
        
        progressPrinter.start(this, data.size() + 3);
        // Init NestedVariablesContainer
        try {
            init(progressPrinter);
        } catch (FileNotFoundException e) {
            throw new FilterException(e);
        } catch (IOException e) {
            throw new FilterException(e);
        }
        
        List<PermanentNestedSmellCandidate> results = new ArrayList<>();
        for (int i = 0, n = data.size(); i < n; i++) {
            if (!(data.get(i) instanceof NestedDependsSmellCandidate)) {
                throw new WrongFilterException(AbsoluteNestedDependsFinder.class, NestedDependsSmellCandidate.class,
                    data.get(i));
            }
            
            NestedDependsSmellCandidate block = (NestedDependsSmellCandidate) data.get(i);
            Set<String> nestedVariables = ConditionUtils.getVariables(block.getInnerCondition());
            Set<String> parentVariables = ConditionUtils.getVariables(block.getOuterCondition());
            for (String nestedVar : nestedVariables) {
                if (permanentNestings.hasPermanentParents(nestedVar)) {
                    Set<String> permanentParents = permanentNestings.getPermanentParents(nestedVar);
                    boolean found = false;
                    Iterator<String> parentItr = parentVariables.iterator();
                    while (parentItr.hasNext() && !found){
                        String parent = parentItr.next();
                        if (permanentParents.contains(parent)) {
                            found = true;
                            results.add(new PermanentNestedSmellCandidate(block, permanentParents, nestedVar));
                        }
                    }
                }
            }
            
            // Dependent inside nested condition?
            progressPrinter.finishedOne();
        }
        Collections.sort(results);
        List<IDataElement> resultWrapper = new ArrayList<>();
        resultWrapper.addAll(results);
        return resultWrapper;
    }

}
