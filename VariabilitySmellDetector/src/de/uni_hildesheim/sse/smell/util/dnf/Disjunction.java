package de.uni_hildesheim.sse.smell.util.dnf;

class Disjunction extends Formula {
    
    private Formula nestedLeft;
    
    private Formula nestedRight;
    
    public Disjunction(Formula nestedLeft, Formula nestedRight) {
        this.nestedLeft = nestedLeft;
        this.nestedRight = nestedRight;
    }
    
    @Override
    public boolean evaluate() {
        return nestedLeft.evaluate() || nestedRight.evaluate();
    }
    
}