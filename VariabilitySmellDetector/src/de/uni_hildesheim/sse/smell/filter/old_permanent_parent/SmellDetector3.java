package de.uni_hildesheim.sse.smell.filter.old_permanent_parent;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sat4j.specs.TimeoutException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uni_hildesheim.sse.smell.IProgressPrinter;
import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.PermanentNestedSmellCandidate;
import de.uni_hildesheim.sse.smell.data.TmpTestSmell;
import de.uni_hildesheim.sse.smell.filter.AbstractSatSolverFilter;
import de.uni_hildesheim.sse.smell.filter.WrongFilterException;
import de.uni_hildesheim.sse.smell.util.ConditionUtils;
import de.uni_hildesheim.sse.smell.util.ConstraintException;
import de.uni_hildesheim.sse.smell.util.VarNotFoundException;

/**
 * This is a {@link SmellDetector} that additionally checks, whether variables
 * are configurable by hand.
 * 
 * @author Adam Krafczyk
 */
public class SmellDetector3 extends AbstractSatSolverFilter {

    private PrintWriter missingVars;

    private Set<String> hasPrompt;
    
    public SmellDetector3(String dimacsModelFile, String missingVarsFile, String rsfModelFile) throws IOException {
        super(dimacsModelFile);
        if (missingVarsFile != null) {
            this.missingVars = new PrintWriter(missingVarsFile);
        }

        try {
            loadRsfPrompts(rsfModelFile);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void loadRsfPrompts(String rsfModelFile) throws ParserConfigurationException, SAXException, IOException {
        hasPrompt = new HashSet<>();
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
                    if (attr != null && attr.getTextContent().equals("prompt")) {
                        hasprompt = true;
                        break;
                    }
                }
            }
            if (hasprompt) {
                hasPrompt.add("CONFIG_" + nodeList.item(x).getTextContent());
            }
            // System.out.println(nodeList.item(x).getTextContent() + " -> " + hasprompt);
        }
    }

    @Override
    public List<IDataElement> run(List<IDataElement> data, IProgressPrinter progressPrinter)
            throws WrongFilterException {
        List<IDataElement> result = new ArrayList<>();

        progressPrinter.start(this, data.size());

        for (IDataElement element : data) {

            if (!(element instanceof PermanentNestedSmellCandidate)) {
                throw new WrongFilterException(SmellDetector3.class, PermanentNestedSmellCandidate.class, data);
            }
            PermanentNestedSmellCandidate candidate = (PermanentNestedSmellCandidate) element;

            try {
                String child = candidate.getPermanentNestedVar();
                String parent = candidate.getOuterCondition();
                
                boolean childAndNotParent = isSolvable("(" + child + ") && !(" + parent + ")");
                boolean notChildAndNotParent = isSolvable("!(" + child + ") && !(" + parent + ")");
                boolean childAndParent = isSolvable("(" + child + ") && (" + parent + ")");
                boolean notChildAndParent = isSolvable("!(" + child + ") && (" + parent + ")");
                
                Set<String> childreen = ConditionUtils.getVariables(child);
                Set<String> parents = ConditionUtils.getVariables(parent);
                boolean oneChildHasPrompt = false;
                boolean oneParentHasPrompt = false;
                for (String tmp : childreen) {
                    if (hasPrompt.contains(tmp)) {
                        oneChildHasPrompt = true;
                        break;
                    }
                }
                for (String tmp : parents) {
                    if (hasPrompt.contains(tmp)) {
                        oneParentHasPrompt = true;
                        break;
                    }
                }
                
                result.add(new TmpTestSmell(candidate,
                        childAndNotParent, notChildAndNotParent,
                        childAndParent, notChildAndParent,
                        oneChildHasPrompt, oneParentHasPrompt));
                
                
            } catch (VarNotFoundException e1) {
                // This is thrown if the code constraints contain variables that
                // are not found in the VarModel.
                if (missingVars != null) {
                    missingVars.println(e1.getName());
                }
                System.out.println("Cannot solve candidate " + candidate.getPermanentNestedVar()
                        + ", because the following variable was not found in the DIMACS model: " + e1.getName());
            } catch (TimeoutException | ConstraintException e1) {
                System.out.println("Cannot solve candidate " + candidate.getPermanentNestedVar()
                        + ", because the following exception occured:");
                e1.printStackTrace(System.out);
            }

            progressPrinter.finishedOne();
        }
        return result;
    }

}
