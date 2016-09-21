package de.uni_hildesheim.sse.smell.data;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class VariableLocation implements IDataElement {

    private String variable;
    
    private List<Location> sourceLocations;
    
    private List<Location> headerLocations;
    
    private List<Location> kconfigLocations;
    
    private List<Location> kbuildLocations;
    
    public VariableLocation(String variable) {
        this.variable = variable;
        sourceLocations = new LinkedList<>();
        headerLocations = new LinkedList<>();
        kconfigLocations = new LinkedList<>();
        kbuildLocations = new LinkedList<>();
    }
    
    public String getVariable() {
        return variable;
    }
    
    public void addLocation(Location location) throws UnkownLocationException {
        if (location.getFile().endsWith(".c") || location.getFile().endsWith(".S")) {
            sourceLocations.add(location);
        } else if (location.getFile().endsWith(".h")) {
            headerLocations.add(location);
        } else if (location.getFile().contains(File.separatorChar + "Kconfig")) {
            kconfigLocations.add(location);
        } else if (location.getFile().contains(File.separatorChar + "Makefile")) {
            kbuildLocations.add(location);
        } else {
            throw new UnkownLocationException(location);
        }
    }
    
    @Override
    public String toCsvLine(String delim) {
        return variable + delim + locationListToString(sourceLocations)
                + delim + locationListToString(headerLocations)
                + delim + locationListToString(kconfigLocations)
                + delim + locationListToString(kbuildLocations);
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
        return "variable;source locations;header locations;kconfig locations;kbuild locations";
    }
    
    private static String locationListToString(List<Location> locations) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < locations.size(); i++) {
            result.append(locations.get(i).toString());
            if (i != locations.size() - 1) {
                result.append(",");
            }
        }
        return result.toString();
    }

    public static class Location {
        
        private String file;
        
        private int lineNumber;
        
        public Location(String file, int lineNumber) {
            this.file = file;
            this.lineNumber = lineNumber;
        }
        
        public String getFile() {
            return file;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        @Override
        public String toString() {
            return file + ":" + lineNumber;
        }
        
    }
    
    public static class UnkownLocationException extends Exception {

        private static final long serialVersionUID = 4381056386888691033L;
        
        UnkownLocationException(Location location) {
            super("Can't categorise file: " + location.getFile());
        }
        
    }
    
}
