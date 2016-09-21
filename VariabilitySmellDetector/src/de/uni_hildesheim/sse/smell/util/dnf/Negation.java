package de.uni_hildesheim.sse.smell.util.dnf;

class Negation extends Formula {
    
    private Formula nested;
    
    public Negation(Formula nested) {
        this.nested = nested;
    }
    
    @Override
    public boolean evaluate() {
        return !nested.evaluate();
    }
    
}
