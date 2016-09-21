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
import de.uni_hildesheim.sse.smell.data.ISmellCandidate;
import de.uni_hildesheim.sse.smell.data.NestedDependsSmellCandidate;
import de.uni_hildesheim.sse.smell.data.PermanentNestedSmellCandidate;
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
public class SmellDetector2 extends AbstractSatSolverFilter {

    private PrintWriter missingVars;

    private Set<String> hasPrompt;

    /**
     * Creates a {@link SmellDetector} for the given VarModel.
     * 
     * @param dimacsModelFile
     *            A file containing the VarModel in DIMACS format.
     * @param missingVarsFile
     *            A filename to write variable names to, which are found in constraints in the code but not in the
     *            VarModel. <code>null</code> to disable.
     * @throws IOException
     *             If reading the model file fails.
     */
    public SmellDetector2(String dimacsModelFile, String missingVarsFile) throws IOException {
        super(dimacsModelFile);
        if (missingVarsFile != null) {
            this.missingVars = new PrintWriter(missingVarsFile);
        }
    }
    
    public SmellDetector2(String dimacsModelFile, String missingVarsFile, String rsfModelFile) throws IOException {
        this(dimacsModelFile, missingVarsFile);

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

            if (!(element instanceof NestedDependsSmellCandidate)) {
                throw new WrongFilterException(SmellDetector.class, NestedDependsSmellCandidate.class, data);
            }
            ISmellCandidate candidate = (ISmellCandidate) element;
            PermanentNestedSmellCandidate permCandidate = (PermanentNestedSmellCandidate) candidate;

            try {
                // If VarModel AND NOT(codeExtract) is solvable, then we have a smell.
                if (isSolvable(candidate.getVarModelCondition())) {


                    boolean prompConditionFulfilled = false;
                    if (hasPrompt != null) {
                        Set<String> childreen = ConditionUtils.getVariables(permCandidate.getPermanentNestedVar());
                        Set<String> parents = ConditionUtils.getVariables(permCandidate.getOuterCondition());
                        boolean oneChildHasPrompt = false;
                        boolean oneParentHasPrompt = false;
                        
                        
                        for (String child : childreen) {
                            if (hasPrompt.contains(child)) {
                                oneChildHasPrompt = true;
                                break;
                            }
                        }
                        for (String parent : parents) {
                            if (hasPrompt.contains(parent)) {
                                oneParentHasPrompt = true;
                                break;
                            }
                        }
//                        System.out.println("Checking...");
//                        if (!oneChildHasPrompt && !oneParentHasPrompt) {
//                            System.out.println("Neither parent {" + parents.toString() + "} nor child{" + childreen.toString() + "}");
//                        } else if (!oneChildHasPrompt) {
//                            System.out.println("No child{" + childreen.toString() + "}");
//                        } else if(!oneParentHasPrompt) {
//                            System.out.println("No parent{" + parents.toString() + "}");
//                        } else {
//                            System.out.println("Adding!");
//                        }
                        
                        prompConditionFulfilled = oneChildHasPrompt && oneParentHasPrompt;
                    } else {
                        prompConditionFulfilled = true;
                    }
                        
                    if (prompConditionFulfilled
                            /*&& isSolvable("!" + permCandidate.getPermanentNestedVar() + " && !("
                            + permCandidate.getOuterCondition() + ")")*/) {
                        result.add(candidate.getSmell(this));
                    }
                }
            } catch (VarNotFoundException e1) {
                // This is thrown if the code constraints contain variables that
                // are not found in the VarModel.
                if (missingVars != null) {
                    missingVars.println(e1.getName());
                }
                System.out.println("Cannot solve candidate " + permCandidate.getPermanentNestedVar()
                        + ", because the following variable was not found in the DIMACS model: " + e1.getName());
            } catch (TimeoutException | ConstraintException e1) {
                System.out.println("Cannot solve candidate " + permCandidate.getPermanentNestedVar()
                        + ", because the following exception occured:");
                e1.printStackTrace(System.out);
            }

            progressPrinter.finishedOne();
        }
        return result;
    }
    
}
