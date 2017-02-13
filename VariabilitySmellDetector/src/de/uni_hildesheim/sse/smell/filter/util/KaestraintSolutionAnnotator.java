package de.uni_hildesheim.sse.smell.filter.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.AnnotatedKaestraintResult;
import de.uni_hildesheim.sse.smell.data.AnnotatedKaestraintResult.Location;
import de.uni_hildesheim.sse.smell.data.AnnotatedKaestraintResult.UnkownLocationException;
import de.uni_hildesheim.sse.smell.data.VariableWithSolutions;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;

/**
 * <b>Input</b>: {@link VariableWithSolutions}s<br />
 * <b>Output</b>: {@link AnnotatedKaestraintResult}s<br />
 * 
 * @author Adam Krafczyk
 */
public class KaestraintSolutionAnnotator implements IFilter {

    private File linuxTree;
    
    private Line[] relevantLines;
    
    private HashSet<String> variablesWithPrompt;
    
    public KaestraintSolutionAnnotator(String linuxTree, String rsfFile) throws Exception {
        this.linuxTree = new File(linuxTree);
        if (!this.linuxTree.isDirectory()) {
            throw new FileNotFoundException(linuxTree + " is not a directory");
        }
        loadRsfPrompts(rsfFile);
    }
    
    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws FilterException {
        List<IDataElement> result = new ArrayList<IDataElement>();
        
        progressPrinter.start("KaestraintSolutionAnnotator - Searching Linux Tree", 1);
        
        List<Line> relevantLinesList = findRelevantLines(linuxTree);
        relevantLines = relevantLinesList.toArray(new Line[] {});
        relevantLinesList = null;
        
        progressPrinter.finishedOne();
        progressPrinter.start(this, data.size());
        
        for (IDataElement element : data) {
            if (!(element instanceof VariableWithSolutions)) {
                throw new WrongFilterException(KaestraintSolutionAnnotator.class, VariableWithSolutions.class, element);
            }
            VariableWithSolutions var = (VariableWithSolutions) element;
            
            String varName = var.getVariable();
            if (varName.endsWith("_MODULE")) {
                varName = varName.substring(0, varName.length() - "_MODULE".length());
            }
            
            boolean hasPrompt = variablesWithPrompt.contains(varName);
            
            AnnotatedKaestraintResult annotatedSmell = new AnnotatedKaestraintResult(var);
            annotatedSmell.setHasPrompt(hasPrompt);
            
            for (Location loc : findLocations(annotatedSmell.getVariable())) {
                try {
                    annotatedSmell.addLocation(loc);
                } catch (UnkownLocationException e) {
                    System.out.println("Can't add location for variable \"" + annotatedSmell.getVariable() + "\": " + e.getMessage());
                }
            }
            
            result.add(annotatedSmell);
            
            progressPrinter.finishedOne();
        }
        
        result.sort(new Comparator<IDataElement>() {

            @Override
            public int compare(IDataElement o1, IDataElement o2) {
                AnnotatedKaestraintResult r1 = (AnnotatedKaestraintResult) o1;
                AnnotatedKaestraintResult r2 = (AnnotatedKaestraintResult) o2;
                return r1.getVariable().compareTo(r2.getVariable());
            }
        });
        
        return result;
    }
    
    private List<Location> findLocations(String variable) {
        List<Location> results = new LinkedList<>();
        
        // don't differentiate between module and normal variable
        if (variable.endsWith("_MODULE")) {
            variable = variable.substring(0, variable.length() - "_MODULE".length());
        }
        
        String kconfigName = variable;
        if (kconfigName.startsWith("CONFIG_")) {
            kconfigName = kconfigName.substring("CONFIG_".length());
        }
        
        
        Pattern kconfigPattern = Pattern.compile("^\\s*(menu)?config\\s*" + kconfigName + "\\s*$");
        Pattern sourcePattern = Pattern.compile(".*" + variable + "(_MODULE)?(([^A-Za-z0-9_].*)|$)");
        
        for (Line line : relevantLines) {
            if (line.filename.contains(File.separatorChar + "Kconfig")) {
                if (kconfigPattern.matcher(line.text).matches()) {
                    results.add(new Location(line.filename, line.lineNumber));
                }
            } else {
                if (sourcePattern.matcher(line.text).matches()) {
                    results.add(new Location(line.filename, line.lineNumber));
                }
            }
        }
        
        return results;
    }
    
    private List<Line> findRelevantLines(File fileTree) {
        List<Line> results = new LinkedList<>();
        
        File[] filtered = fileTree.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory()
                        || pathname.getName().endsWith(".c")
                        || pathname.getName().endsWith(".S")
                        || pathname.getName().endsWith(".h")
                        || pathname.getName().startsWith("Kconfig")
                        || pathname.getName().startsWith("Makefile")
                        || pathname.getName().startsWith("Kbuild");
            }
        });
        
        for (File file : filtered) {
            if (file.isDirectory()) {
                results.addAll(findRelevantLines(file));
            } else {
                try {
                    if (file.getName().startsWith("Kconfig")) {
                        results.addAll(findRelevantLinesInFile("^\\s*(menu)?config\\s*[A-Za-z0-9_]+\\s*$", file));
                    } else {
                        results.addAll(findRelevantLinesInFile(".*CONFIG_.*", file));
                    }
                } catch (IOException e) {
                    System.out.println("Can't search in file \"" + file.getName() + "\":");
                    e.printStackTrace(System.out);
                }
            }
        }
        
        return results;
    }
    
    private List<Line> findRelevantLinesInFile(String regex, File file) throws IOException {
        List<Line> results = new LinkedList<>();
        
        Pattern pattern = Pattern.compile(regex);
        
        BufferedReader in = new BufferedReader(new FileReader(file));
        
        String line;
        
        int lineNumber = 1;
        while ((line = in.readLine()) != null) {
            if (pattern.matcher(line).matches()) {
                results.add(new Line(relativeName(file, linuxTree), lineNumber, line));
            }
            
            lineNumber++;
        }
        
        in.close();
        
        return results;
    }
    
    private static String relativeName(File file, File directory) {
        return file.getAbsolutePath().substring(directory.getAbsolutePath().length());
    }
    
    private static final class Line {
        
        private String filename;
        
        private int lineNumber;
        
        private String text;

        public Line(String filename, int lineNumber, String text) {
            this.filename = filename;
            this.lineNumber = lineNumber;
            this.text = text;
        }
        
        
    }
    
    private void loadRsfPrompts(String rsfModelFile) throws ParserConfigurationException, SAXException, IOException {
        variablesWithPrompt = new HashSet<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new File(rsfModelFile));
        NodeList nodeList = document.getElementsByTagName("name");
        for (int x = 0; x < nodeList.getLength(); x++) {
            // search prompt
            Node node = nodeList.item(x);
            boolean hasprompt = false;
            while ((node = node.getNextSibling()) != null) {
                if (node.hasAttributes()) {
                    Node attr = node.getAttributes().getNamedItem("type");
                    if (attr != null) {
                        String type = attr.getTextContent();
                        if (type.equals("prompt") || type.equals("menu")) {
                            hasprompt = true;
                            break;
                        }
                        
                    }
                }
            }
            if (hasprompt) {
                variablesWithPrompt.add("CONFIG_" + nodeList.item(x).getTextContent());
            }
            // System.out.println(nodeList.item(x).getTextContent() + " -> " + hasprompt);
        }
    }

}
