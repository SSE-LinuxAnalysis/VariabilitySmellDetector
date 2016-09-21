package de.uni_hildesheim.sse.smell.test.util;

import de.uni_hildesheim.sse.smell.util.MaxTermConverterWrapper;

public class MaxTermConverterTest extends AbstractCnfConverterTest {

    public MaxTermConverterTest() {
        super(new MaxTermConverterWrapper());
    }

}
