package de.uni_hildesheim.sse.smell.filter.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariableWithSolutions;
import de.uni_hildesheim.sse.smell.filter.FilterException;
import de.uni_hildesheim.sse.smell.filter.IFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;

/**
 * <b>Input</b>: {@link VariableWithSolutions}s<br />
 * <b>Output</b>: {@link VariableWithSolutions}s<br />
 * 
 * @author Adam Krafczyk
 */
public class VisibleVariableFilter implements IFilter {

    private HashSet<String> variablesWithPrompt;
    
    private boolean outputWithPrompt;
    
    public VisibleVariableFilter(String rsfFile, boolean outputWithPrompt) throws ParserConfigurationException, SAXException, IOException {
        this.outputWithPrompt = outputWithPrompt;
        loadRsfPrompts(rsfFile);
    }
    
    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter) throws FilterException {
        List<IDataElement> result = new ArrayList<IDataElement>();
        progressPrinter.start(this, data.size());
        
        
        for (IDataElement element : data) {
            if (!(element instanceof VariableWithSolutions)) {
                throw new WrongFilterException(VisibleVariableFilter.class, VariableWithSolutions.class, element);
            }
            VariableWithSolutions var = (VariableWithSolutions) element;
            
            String varName = var.getVariable();
            if (varName.endsWith("_MODULE")) {
                varName = varName.substring(0, varName.length() - "_MODULE".length());
            }
            
            boolean hasPrompt = variablesWithPrompt.contains(varName);
            
            if (outputWithPrompt == hasPrompt) {
                result.add(var);
            }
            
            progressPrinter.finishedOne();
        }
        
        return result;
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
