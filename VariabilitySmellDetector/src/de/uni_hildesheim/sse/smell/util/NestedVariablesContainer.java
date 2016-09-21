package de.uni_hildesheim.sse.smell.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Sascha El-Shakarwy
 */
public class NestedVariablesContainer {
    
    private Map<String, Set<String>> nestedInMap;
    
    public NestedVariablesContainer() {
        nestedInMap = new HashMap<>();
    }
    
    public void addNesting(String nestedVar, Set<String> parents) {
        Set<String> permanentParents = nestedInMap.get(nestedVar);
        if (null == permanentParents) {
            // Variable was not considered before -> create minimal list of parents by means of current list
            permanentParents = new HashSet<String>(parents);
            nestedInMap.put(nestedVar, permanentParents);
        } else {
            // Create intersection
            permanentParents.retainAll(parents);
        }
    }
    
    public void clearParents(String variable) {
        addNesting(variable, new HashSet<String>());
    }
    
    public boolean hasPermanentParents(String variable) {
        Set<String> permanentParents = nestedInMap.get(variable);
        return null != permanentParents && !permanentParents.isEmpty();
    }
    
    public Set<String> getPermanentParents(String variable) {
        return nestedInMap.get(variable);
    }
    
    public Set<Map.Entry<String, Set<String>>> getPermanentNestedVars() {
        Set<Map.Entry<String, Set<String>>> results = new HashSet<>();
        for (Entry<String, Set<String>> entry : nestedInMap.entrySet()) {
            if (hasPermanentParents(entry.getKey())) {
                results.add(entry);
            }
        }
        
        return results;
    }
    
    @Override
    public String toString() {
        return nestedInMap.toString();
    }
}
