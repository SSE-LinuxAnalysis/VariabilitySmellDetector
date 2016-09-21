package de.uni_hildesheim.sse.smell.util.dnf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Solution {

    private Map<String, Boolean> values;
    
    public Solution() {
        values = new HashMap<>();
    }
    
    public void setValue(String var, boolean value) {
        values.put(var, value);
    }
    
    public Set<String> getVariables() {
        return values.keySet();
    }
    
    public boolean getValue(String var) {
        return values.get(var);
    }
    
    public void removeValue(String var) {
        values.remove(var);
    }
    
    public boolean equals(Solution other) {
        return this.values.equals(other.values);
    }
    
    public boolean hasSameVariables(Solution other) {
        return this.values.keySet().equals(other.values.keySet());
    }
    
    /**
     * Calculates the variables that differ between this solution and the other one.
     * Both solutions must contain the same variables.
     * 
     * @param other The other solution to check against.
     * 
     * @return A set of variables, that have different values in both solutions.
     */
    public Set<String> getDifference(Solution other) {
        Set<String> difference = new HashSet<>();
        
        for (String var : this.values.keySet()) {
            if (this.getValue(var) != other.getValue(var)) {
                difference.add(var);
            }
        }
        
        return difference;
    }
    
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        
        List<Map.Entry<String, Boolean>> sorted = new ArrayList<>(values.entrySet());
        Collections.sort(sorted, new Comparator<Map.Entry<String, Boolean>>() {

            @Override
            public int compare(Entry<String, Boolean> o1, Entry<String, Boolean> o2) {
                String name1 = o1.getKey();
                String name2 = o2.getKey();
                
                if (name1.endsWith("_MODULE")) {
                    name1 = name1.substring(0, name1.length() - "_MODULE".length()) + ' ';
                }
                if (name2.endsWith("_MODULE")) {
                    name2 = name2.substring(0, name2.length() - "_MODULE".length()) + ' ';
                }
                
                return name1.compareTo(name2);
            }
        });
        
        for (Map.Entry<String, Boolean> entry : sorted) {
            if (!entry.getValue()) {
                result.append('!');
            }
            result.append(entry.getKey()).append(' ');
        }
        
        return result.toString();
    }
    
}
