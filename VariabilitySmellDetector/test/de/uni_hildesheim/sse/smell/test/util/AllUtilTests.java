package de.uni_hildesheim.sse.smell.test.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.uni_hildesheim.sse.smell.test.util.dnf.AllDnfTests;

@RunWith(Suite.class)
@SuiteClasses({ AllDnfTests.class, AllCnfConverterTests.class, ConditionUtilsTest.class, VariableToNumberConverterTest.class })
public class AllUtilTests {

}
