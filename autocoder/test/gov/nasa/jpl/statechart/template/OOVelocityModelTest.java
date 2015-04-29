/**
 * Created Sep 29, 2009.
 * <p>
 * Copyright 2009, by the California Institute of Technology. ALL RIGHTS
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

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.UMLModelGroup;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test class to test that OOVelocityModel functions produce the expected
 * results, and that velocity template strings against corresponding functions
 * work properly.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class OOVelocityModelTest extends AbstractVelocityModelTest<OOVelocityModel> {

    @Override
    protected OOVelocityModel instantiateVelocityModel (UMLModelGroup modelGrp, StateMachine machine) {
        return new OOVelocityModel(machine);
    }


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass () throws Exception {
        AbstractVelocityModelTest.setUpBeforeClass();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass () throws Exception {
        AbstractVelocityModelTest.tearDownAfterClass();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp () throws Exception {
        // set up the default test snippet
        prepareUmlSnippet(SNIPPET_COMPLEX_SM);
        numRegionAndStates = 23;  // expediency: hard-coded for Complex SM
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown () throws Exception {
    }


    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getStatesBreadthFirst(java.util.Entry, boolean)}.
     * Checks that the velocity model properly retrieves all states within the
     * machine in a breadth-first ordering.
     */
    @Test
    public void testGetStatesBreadthFirst () {
        // test on the default loaded one first
        doTestGetStatesBreadthFirst();

        prepareUmlSnippet(SNIPPET_SMAPMODES_SM);
        numRegionAndStates = 40;
        doTestGetStatesBreadthFirst();
    }

    private void doTestGetStatesBreadthFirst () {
        // get states in breadth-first ordering via the velocity model
        StateMachine machine = getFirstStateMachine(model);
        Collection<State> states = velomodel.getStatesBreadthFirst(machine, false);
        List<State> statesToCheck = new ArrayList<State>();
        for (State state : states) {
            statesToCheck.add(state);
        }

        // manually find all states within machine
        //  (this snippet of algorithm turns out to be breadth first!)
        Queue<Region> regionsToCheck = new LinkedList<Region>();
        List<State> refStates = new ArrayList<State>();
        regionsToCheck.addAll(machine.getRegion());
        while (! regionsToCheck.isEmpty()) {
            Region region = regionsToCheck.poll();
            for (State s : Util.filter(region.getSubvertex(), State.class)) {
                refStates.add(s);
                // add more sub regions to check
                regionsToCheck.addAll(s.getRegion());
            }
        }

        // test that the two sets are order-equivalent
        Assert.assertEquals("Set of states not equivalent!",
                refStates,
                statesToCheck);

        // test for template retrieval of states, compare using set toStrings
        Set<String> refStateStrings = new HashSet<String>();
        for (State s : refStates) {
            refStateStrings.add(s.toString());
        }
        Assert.assertEquals("Template test: set of states not equivalent!",
                refStateStrings,
                arrayStringToSet(evalTemplate("$model.getStatesBreadthFirst($model.stateMachine, false)")));
    }

//    /**
//     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getAllStatesBreadthFirst(gov.nasa.jpl.statechart.uml.StateMachine)}.
//     * Checks that the count of all state machines, states, and regions
//     * returned via the velocity model is the same as via the UML model.
//     */
//    @Test
//    public void testGetAllStatesBreadthFirst () {
//        // test on the default loaded one first
//        doTestGetAllStatesBreadthFirst();
//
//        prepareUmlSnippet(SNIPPET_SMAPMODES_SM);
//        numRegionAndStates = 38;
//        doTestGetAllStatesBreadthFirst();
//    }
//
//    private void doTestGetAllStatesBreadthFirst () {
//        // get "allStates" from the velocity model
//        Map<String,NamedElement> allNamespaces = velomodel.getAllStatesBreadthFirst();
//        System.err.println("** Showing list of states & regions visited breadth-first:");
//        for (NamedElement ne : allNamespaces.values()) {
//            System.err.println(ne.getQualifiedName());
//        }
//        // make sure we have unique instances
//        Set<NamedElement> uniqueNamespaces = new HashSet<NamedElement>(allNamespaces.values());
//        // test that the 2 sets are equivalent
//        Assert.assertEquals("Count of all states and regions not equal!",
//                numRegionAndStates,
//                uniqueNamespaces.size());
//
//        // test for template eval outcome
//        Assert.assertEquals("Template test: count of all states and regions unequal!",
//                uniqueNamespaces.size(),
//                arrayStringToSet(evalTemplate("$model.getAllStatesBreadthFirst().values()")).size());
//    }

}
