package de.uni_hildesheim.sse.smell.util.dnf;

class Variable extends Formula {
    
    private String name;
    
    private boolean value;
    
    public Variable(String name) {
        this.name = name;
    }
    
    public void setValue(boolean value) {
        this.value = value;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean evaluate() {
        return value;
    }
    
}
