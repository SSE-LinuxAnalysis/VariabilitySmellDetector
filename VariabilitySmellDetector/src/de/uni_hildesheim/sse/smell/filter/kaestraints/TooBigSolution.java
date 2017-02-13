package de.uni_hildesheim.sse.smell.filter.kaestraints;

import java.util.HashSet;
import java.util.Set;

import de.uni_hildesheim.sse.smell.util.dnf.Solution;

public class TooBigSolution extends Solution {


    @Override
    public void setValue(String var, boolean value) {
    }
    
    @Override
    public Set<String> getVariables() {
        return new HashSet<>();
    }
    
    @Override
    public boolean getValue(String var) {
        throw new NullPointerException();
    }
    
    @Override
    public void removeValue(String var) {
    }
    
    @Override
    public boolean equals(Solution other) {
        return other instanceof TooBigSolution;
    }
    
    @Override
    public boolean hasSameVariables(Solution other) {
        return other instanceof TooBigSolution;
    }
    
    @Override
    public Set<String> getDifference(Solution other) {
        return other.getVariables();
    }
    
    @Override
    public String toString() {
        return "Too big to find solutions";
    }
    
}
