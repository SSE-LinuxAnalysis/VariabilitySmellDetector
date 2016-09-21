package de.uni_hildesheim.sse.smell.data;

public class TmpTestSmell implements IDataElement {

    private boolean childAndNotParent;
    private boolean notChildAndNotParent;
    private boolean childAndParent;
    private boolean notChildAndParent;
    private boolean oneChildHasPrompt;
    private boolean oneParentHasPrompt;
    
    private String child;
    
    private String parent;
    
    public TmpTestSmell(PermanentNestedSmellCandidate candidate,
            boolean childAndNotParent, boolean notChildAndNotParent,
            boolean childAndParent, boolean notChildAndParent,
            boolean oneChildHasPrompt, boolean oneParentHasPrompt) {
        this.childAndNotParent = childAndNotParent;
        this.notChildAndNotParent = notChildAndNotParent;
        this.childAndParent = childAndParent;
        this.notChildAndParent = notChildAndParent;
        this.oneChildHasPrompt = oneChildHasPrompt;
        this.oneParentHasPrompt = oneParentHasPrompt;
        this.child = candidate.getPermanentNestedVar();
        this.parent = candidate.getOuterCondition();
    }
    
    @Override
    public String toCsvLine(String delim) {
        return child + delim + parent + delim + childAndNotParent
                + delim + notChildAndNotParent + delim + childAndParent
                + delim + notChildAndParent + delim + oneChildHasPrompt
                + delim + oneParentHasPrompt;
    }

    @Override
    public String toCsvLine() {
        return toCsvLine(";");
    }

    @Override
    public String headertoCsvLine() {
        return headertoCsvLine(";");
    }

    @Override
    public String headertoCsvLine(String delim) {
        return "child" + delim + "parent" + delim + "child && !parent"
                + delim + "!child && !parent" + delim + "child && parent"
                + delim + "!child && parent" + delim + "child prompt"
                + delim + "parent prompt";
    }

}
