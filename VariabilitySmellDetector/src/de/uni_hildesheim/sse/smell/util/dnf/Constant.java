package de.uni_hildesheim.sse.smell.util.dnf;

public class Constant extends Formula {

    private boolean value;
    
    public Constant(boolean value) {
        this.value = value;
    }
    
    @Override
    public boolean evaluate() {
        return value;
    }
    
}
