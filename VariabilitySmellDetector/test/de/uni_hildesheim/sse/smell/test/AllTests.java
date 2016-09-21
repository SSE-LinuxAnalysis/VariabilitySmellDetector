package de.uni_hildesheim.sse.smell.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.uni_hildesheim.sse.smell.test.filter.AllFilterTests;
import de.uni_hildesheim.sse.smell.test.util.AllUtilTests;

@RunWith(Suite.class)
@SuiteClasses({AllUtilTests.class, AllFilterTests.class})
public class AllTests {

}
