/**
 * Created Apr 6, 2010.
 * <p>
 * Copyright 2009-2010, by the California Institute of Technology. ALL RIGHTS
 * RESERVED. United States Government Sponsorship acknowledged. Any commercial
 * use must be negotiated with the Office of Technology Transfer at the
 * California Institute of Technology.
 * </p>
 * <p>
 * This software is subject to U.S. export control laws and regulations and has
 * been classified as 4D993. By accepting this software, the user agrees to
 * comply with all applicable U.S. export laws and regulations. User has the
 * responsibility to obtain export licenses, or other export authority as may be
 * required before exporting such information to foreign countries or providing
 * access to foreign persons.
 * </p>
 */
package gov.nasa.jpl.statechart.template;

import gov.nasa.jpl.statechart.uml.UMLConstraint;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Unit test class to test the FunctionCall class and make sure method call
 * strings and their arguments are properly parsed.
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class FunctionCallTest {
    public static final String[] testStrings = {
            "guard1"  /* not a function call */,
            "action1()"  /* simple function call */,
            "action2(1,2,3)"  /* 3 args */,
            "action3(x,,)"  /* 3 args, 2nd and 3rd one empty */,
            "action4(,3)"  /* 2 args, 1st one empty */,
            "action5(a,'')"  /* 2 args, one a single-quoted string */,
            "action6(b,\"\")"  /* 2 args, one a double-quoted string*/,
            "action7(\"x,y\",42,\"string\",0)"  /* complex args with strings */,
            "actionX(\"crazy(a,1,'')\")"  /* function call within a string arg */,
            "actionX(y)"  /* test string to compare equality */
    };
    /**
     * Results are String arrays corresponding positionally to the testString,
     * with zeroth element being the method name,
     * and the i-th element being i-th argument.
     */
    public static final String[][] testResults = {
            { "guard1" },
            { "action1" },
            { "action2", "1", "2", "3" },
            { "action3", "x", "", "" },
            { "action4", "", "3" },
            { "action5", "a", "\'\'" },
            { "action6", "b", "\"\"" },
            { "action7", "\"x,y\"", "42", "\"string\"", "0" },
            { "actionX", "\"crazy(a,1,'')\"" },
            { "actionX", "y" }
    };

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.FunctionCall#isFunctionCall(java.lang.String)}.
     */
    @Test
    public void testIsFunctionCall () {
        Assert.assertFalse("Incorrect isFunction() logic on " + testStrings[0] + "!",
                FunctionCall.isFunctionCall(testStrings[0]));
        for (int i=1; i < testStrings.length; ++i) {
            Assert.assertTrue("Incorrect isFunction() logic on " + testStrings[i] + "!",
                    FunctionCall.isFunctionCall(testStrings[i]));
        }
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.FunctionCall#FunctionCall(java.lang.String)}.
     */
    @Test
    public void testFunctionCallString () {
        FunctionCall fc = new FunctionCall(testStrings[1]);
        Assert.assertFalse("Guard flag should be false for " + testStrings[1] + "!",
                fc.isGuard());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.FunctionCall#FunctionCall(java.lang.String, boolean)}.
     */
    @Test
    public void testFunctionCallStringBoolean () {
        // test guard as true
        FunctionCall fc = new FunctionCall(testStrings[1], new UMLConstraint(null, null));
        Assert.assertTrue("Guard flag should be true for " + testStrings[1] + "!",
                fc.isGuard());

        // test guard as false
        fc = new FunctionCall(testStrings[1], null);
        Assert.assertFalse("Guard flag should be false for " + testStrings[1] + "!",
                fc.isGuard());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.FunctionCall#hashCode()}.
     */
    @Test
    public void testHashCode () {
        // obvious equality
        FunctionCall fc1 = new FunctionCall(testStrings[1]);
        FunctionCall fc2 = new FunctionCall(testStrings[1]);
        Assert.assertTrue("Hash codes for " + fc1.toString() + " <> " + fc2.toString() + "!",
                fc1.hashCode() == fc2.hashCode());

        // equality by function name
        fc1 = new FunctionCall(testStrings[8]);
        fc2 = new FunctionCall(testStrings[9]);
        Assert.assertTrue("Hash codes for " + fc1.toString() + " <> " + fc2.toString() + "!",
                fc1.hashCode() == fc2.hashCode());

        // inequality
        fc1 = new FunctionCall(testStrings[1]);
        fc2 = new FunctionCall(testStrings[8]);
        Assert.assertFalse("Hash codes for " + fc1.toString() + " == " + fc2.toString() + "!",
                fc1.hashCode() == fc2.hashCode());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.FunctionCall#equals(java.lang.Object)}.
     */
    @Test
    public void testEqualsObject () {
        // obvious equality
        FunctionCall fc1 = new FunctionCall(testStrings[1]);
        FunctionCall fc2 = new FunctionCall(testStrings[1]);
        Assert.assertTrue(fc1.toString() + " <> " + fc2.toString() + "!",
                fc1.equals(fc2));

        // equality by function name
        fc1 = new FunctionCall(testStrings[8]);
        fc2 = new FunctionCall(testStrings[9]);
        Assert.assertTrue(fc1.toString() + " <> " + fc2.toString() + "!",
                fc1.equals(fc2));

        // inequality
        fc1 = new FunctionCall(testStrings[1]);
        fc2 = new FunctionCall(testStrings[8]);
        Assert.assertFalse(fc1.toString() + " == " + fc2.toString() + "!",
                fc1.equals(fc2));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.FunctionCall#compareTo(gov.nasa.jpl.statechart.template.FunctionCall)}.
     * Tests that two instances from the same string is equal, but the same
     * two instances with different guard flag is not equal.  Also tests proper
     * ordering by name.
     */
    @Test
    public void testCompareTo () {
        // test equality of same method string
        FunctionCall fc1 = new FunctionCall(testStrings[1]);
        FunctionCall fc2 = new FunctionCall(testStrings[1]);
        Assert.assertTrue(fc1.toString() + " <> " + fc2.toString() + "!",
                fc1.compareTo(fc2) == 0);

        // test inequality with guard flag changed
        fc1.setGuard(new UMLConstraint(null, null));
        Assert.assertTrue(fc1.toString() + " !< " + fc2.toString() + "!",
                fc1.compareTo(fc2) < 0);

        // test ordering by name, action1 < action2
        fc1 = new FunctionCall(testStrings[1]);
        fc2 = new FunctionCall(testStrings[2]);
        Assert.assertTrue(fc1.toString() + " !< " + fc2.toString() + "!",
                fc1.compareTo(fc2) < 0);

        // test ordering by name, actionX > action1
        fc1 = new FunctionCall(testStrings[8]);
        fc2 = new FunctionCall(testStrings[1]);
        Assert.assertTrue(fc1.toString() + " !> " + fc2.toString() + "!",
                fc1.compareTo(fc2) > 0);
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.FunctionCall#argStr()}.
     */
    @Test
    public void testArgStr () {
        FunctionCall fc = new FunctionCall(testStrings[7]);
        Assert.assertTrue(fc.toString() + " arg string incorrect!",
                "\"x,y\",42,\"string\",0".equals(fc.argStr()));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.FunctionCall#setGuard(boolean)}.
     * Test function call instantiated with guard as false, later set to true.
     */
    @Test
    public void testSetGuardSpecification () {
        FunctionCall fc = new FunctionCall(testStrings[2]);
        Assert.assertFalse("Guard flag should be false at first for " + testStrings[2] + "!",
                fc.isGuard());
        fc.setGuard(new UMLConstraint(null, null));
        Assert.assertTrue("Guard flag should be set to true for " + testStrings[2] + "!",
                fc.isGuard());
    }

    /**
     * Test method to test the overall functionality of FunctionCall, with
     * emphasis on proper parsing of arguments.  This test method exercises:
     * {@link gov.nasa.jpl.statechart.template.FunctionCall#FunctionCall(java.lang.String)},
     * {@link gov.nasa.jpl.statechart.template.FunctionCall#name()},
     * {@link gov.nasa.jpl.statechart.template.FunctionCall#hasArgs()}, and
     * {@link gov.nasa.jpl.statechart.template.FunctionCall#argList()}.
     */
    @Test
    public void testFunctionCallArgs () {
        for (int i=0; i < testStrings.length; ++i) {
            FunctionCall fc = new FunctionCall(testStrings[i]);
            // test name()
            Assert.assertTrue(fc.toString() + " name is NOT " + testResults[i][0] + "?!",
                    testResults[i][0].equals(fc.name()));
            // test hasArgs()
            Assert.assertTrue(fc.toString() + " should "
                    + (testResults[i].length <= 1? "NOT ":"") + "have arg(s)!",
                    fc.hasArgs() == testResults[i].length > 1);

            // test argList(), and thus, argument parsing
            String[] parsedArgsList = fc.argList();
            for (int j=1; j < testResults[i].length; ++j) {
                Assert.assertTrue(fc.toString() + " arguments incorrectly parsed: "
                        + Arrays.toString(parsedArgsList) + "!",
                        testResults[i].length-1 == parsedArgsList.length);
                Assert.assertTrue(fc.toString() + " argument "
                        + testResults[i][j] + "incorrectly parsed: "
                        + parsedArgsList[j-1] + "!",
                        testResults[i][j].equals(parsedArgsList[j-1]));
            }
        }
    }

}
