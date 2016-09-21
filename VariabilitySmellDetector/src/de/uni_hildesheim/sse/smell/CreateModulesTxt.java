package de.uni_hildesheim.sse.smell;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateModulesTxt {

    public static void main(String[] args) throws Throwable {
        Set<String> modules = new HashSet<>();
        for (String file : args) {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;
            Pattern p = Pattern.compile("^c (\\d+) (.+)$");
            while ((line = in.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    String var = m.group(2);
                    if (var.endsWith("_MODULE")) {
                        modules.add("CONFIG_" + var);
                    }
                }
            }
            in.close();
        }
       
        
        for (String s : modules) {
            System.out.println(s);
        }
    }

}
