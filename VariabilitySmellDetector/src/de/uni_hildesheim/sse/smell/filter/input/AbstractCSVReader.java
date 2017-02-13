package de.uni_hildesheim.sse.smell.filter.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class AbstractCSVReader {

    private static final String DEFAULT_DELIMITER = ";";
    
    private BufferedReader in;
    private String delimiter;
    
    public AbstractCSVReader(String path) throws FileNotFoundException {
        this(path, DEFAULT_DELIMITER);
    }

    public AbstractCSVReader(String path, String delimiter) throws FileNotFoundException {
        this(new FileInputStream(new File(path)), delimiter);
    }
    
    public AbstractCSVReader(InputStream in) {
        this(in, DEFAULT_DELIMITER);
    }
    
    public AbstractCSVReader(InputStream in, String delimiter) {
        this.in = new BufferedReader(new InputStreamReader(in));
        this.delimiter = delimiter;
    }
    
    public void read(boolean skipFirstLine) throws IOException {
        // Open file if reading starts and not when constructor is called.
        try {
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
