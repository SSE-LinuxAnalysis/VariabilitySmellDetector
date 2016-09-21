package de.uni_hildesheim.sse.smell.filter.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public abstract class AbstractCSVReader {

    private static final String DEFAULT_DELIMITER = ";";
    
    private File file;
    private String delimiter;
    
    public AbstractCSVReader(String path) throws FileNotFoundException {
        this(path, DEFAULT_DELIMITER);
    }

    public AbstractCSVReader(String path, String delimiter) throws FileNotFoundException {
        file = new File(path);
        this.delimiter = delimiter;
        if (null == file || !file.exists()) {
            throw new FileNotFoundException("File \"" + path + "\" does not exist.");
        }
    }
    
    public void read(boolean skipFirstLine) throws IOException {
        // Open file if reading starts and not when constructor is called.
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            String line;
            if (skipFirstLine) {
                in.readLine();
            }
            while ((line = in.readLine()) != null) {
                String[] fields = line.split(delimiter);
                readLine(fields);
            }
        } finally {
            if  (in != null) {
                in.close();
            }
        }
    }
    
    public abstract void readLine(String[] fields);
}
