/**
 * Created Aug 11, 2009.
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

import static org.junit.Assert.fail;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.UMLModelGroup;
import gov.nasa.jpl.statechart.uml.Event;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.Signal;
import gov.nasa.jpl.statechart.uml.SignalEvent;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;
import gov.nasa.jpl.statechart.uml.Transition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test class to test that VelocityModel functions produce the expected
 * results, and that velocity template strings against corresponding functions
 * work properly.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class VelocityModelTest extends AbstractVelocityModelTest<VelocityModel> {

    @Override
    protected VelocityModel instantiateVelocityModel (UMLModelGroup modelGroup, StateMachine machine) {
        return new VelocityModel(machine);
    }

    protected <T extends NamedElement> gov.nasa.jpl.statechart.Entry<String,T> makeEntry (T item) {
        String qname = null;
        List<String> names = velomodel.ns2qname.get(item);
        if (names != null && names.size() > 0) {
            qname = names.get(0);
        }
        if (qname == null) {
            qname = item.getQualifiedName();
        }
        return new gov.nasa.jpl.statechart.Entry<String,T>(qname, item);
    }

    /**
     * Returns the qualified-name/namespace pair for a named state.
     * @return
     */
    protected Entry<String,State> getState (String name) {
        for (Entry<String,State> e
                : velomodel.getStates(velomodel.getStatemachine(), false).entrySet()) {
            if (e.getValue().getName().equals(name)) {
                return e;
            }
        }
        fail("State " + name + " not found, incorrect XML input?!");
        return null;
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
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#sort(java.util.Map)}.
     * Test for sort functionality does not appear necessary since it's
     * implemented using Java's Collections.  However, we at least test to make
     * sure that sort called via the velocity model produces the same outcome. 
     */
    @Test
    public void testSort () {
        // get all the states and toss it through sort
        StateMachine machine = getFirstStateMachine(model);
        SortedMap<?,?> refSorted = new TreeMap<String,NamedElement>(velomodel.getAllStates(machine));

        // test that the template sorted outcome is equivalent by keys AND values
        Assert.assertEquals("Template test: sorted map key set not equal!",
                refSorted.keySet().toString(),
                evalTemplate("$model.sort($model.getAllStates($model.statemachine.value)).keySet()"));
        Assert.assertEquals("Template test: sorted map value set not equal!",
                refSorted.values().toString(),
                evalTemplate("$model.sort($model.getAllStates($model.statemachine.value)).values()"));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#VelocityModel(gov.nasa.jpl.statechart.uml.StateMachine, gov.nasa.jpl.statechart.uml.Model, gov.nasa.jpl.statechart.template.TargetLanguageMapper)}.
     * Makes sure that the qname to ns mapping and reverse mappings check out.
     */
    @Test
    public void testVelocityModel () {
        boolean atLeastOneChecked = false;
        for (Entry<NamedElement,List<String>> e : velomodel.ns2qname.entrySet()) {
            for (String qname : e.getValue()) {
                atLeastOneChecked = true;
                Assert.assertEquals("Found an inconsistent qname-ns mapping!",
                        e.getKey(),
                        velomodel.qname2ns.get(qname));
            }
        }
        // make sure at least one was checked
        Assert.assertTrue("No mapping entry checked at all!",
                atLeastOneChecked);

        // test that $model retrieves the velocity model
        Assert.assertEquals("Template test: model object not equal!",
                velomodel.toString(),
                evalTemplate("$model"));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getStatemachine()}.
     */
    @Test
    public void testGetStatemachine () {
        // get the state machine via the velocity model and test same instance
        StateMachine machine = getFirstStateMachine(model);
        Assert.assertEquals("Machine instance doesn't match!",
                machine,
                velomodel.getStatemachine().getValue());

        // test that $model.statemachine retrieves the statemachine
        Assert.assertEquals("Template test: machine instance doesn't match!",
                machine.toString(),
                evalTemplate("$model.statemachine.value"));
    }

    /**
     * Test method for {@link VelocityModel#getRegions(Entry, boolean)}.
     */
    @Test
    public void testGetRegions () {
        // get the regions via the velocity model
        StateMachine machine = getFirstStateMachine(model);
        Map<String,Region> regions = velomodel.getRegions(makeEntry(machine), false);
        // manually find all regions within machine
        Queue<Region> checkQue = new LinkedList<Region>();
        Set<Region> refRegions = new HashSet<Region>();
        checkQue.addAll(machine.getRegion());
        while (! checkQue.isEmpty()) {
            Region region = checkQue.poll();
            refRegions.add(region);
            for (State s : Util.filter(region.getSubvertex(), State.class)) {
                // add more sub regions to check
                checkQue.addAll(s.getRegion());
            }
        }
        // test that the two sets are equivalent
        Set<Region> regionsToCheck = new HashSet<Region>(regions.values());
        Assert.assertEquals("Set of regions not equivalent!",
                refRegions,
                regionsToCheck);

        // test for template retrieval of regions, compare using set toStrings
        Set<String> refRegionStrings = new HashSet<String>();
        for (Region s : refRegions) {
            refRegionStrings.add(s.toString());
        }
        Assert.assertEquals("Template test: set of regions not equivalent!",
                refRegionStrings,
                arrayStringToSet(evalTemplate("$model.getRegions($model.statemachine, false).values()")));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getStates(java.util.Entry, boolean)}.
     * Checks that the velocity model retrieves all states within the machine.
     */
    @Test
    public void testGetStates () {
        // get the states via the velocity model
        StateMachine machine = getFirstStateMachine(model);
        Map<String,State> states = velomodel.getStates(makeEntry(machine), false);
        // manually find all states within machine
        Queue<Region> regionsToCheck = new LinkedList<Region>();
        Set<State> refStates = new HashSet<State>();
        regionsToCheck.addAll(machine.getRegion());
        while (! regionsToCheck.isEmpty()) {
            Region region = regionsToCheck.poll();
            for (State s : Util.filter(region.getSubvertex(), State.class)) {
                refStates.add(s);
                // add more sub regions to check
                regionsToCheck.addAll(s.getRegion());
            }
        }
        // test that the two sets are equivalent
        Set<State> statesToCheck = new HashSet<State>(states.values());
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
                arrayStringToSet(evalTemplate("$model.getStates($model.statemachine, false).values()")));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getAllStates(gov.nasa.jpl.statechart.uml.StateMachine)}.
     * Checks that the count of all state machines, states, and regions
     * returned via the velocity model is the same as via the UML model.
     */
    @Test
    public void testGetAllStates () {
        // get "allStates" from the velocity model
        StateMachine machine = getFirstStateMachine(model);
        Map<String,NamedElement> allNamespaces = velomodel.getAllStates(machine);
        // make sure we have unique instances
        Set<NamedElement> uniqueNamespaces = new HashSet<NamedElement>(allNamespaces.values());
        // test that the 2 sets are equivalent
        Assert.assertEquals("Count of all states and regions not equal!",
                numRegionAndStates,
                uniqueNamespaces.size());

        // test for template eval outcome
        Assert.assertEquals("Template test: count of all states and regions unequal!",
                uniqueNamespaces.size(),
                arrayStringToSet(evalTemplate("$model.getAllStates($model.statemachine.value).values()")).size());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getInitialState(java.util.Entry)}.
     */
    @Test
    public void testGetInitialState () {
        // get initial state from velocity model
        StateMachine machine = getFirstStateMachine(model);
        Pseudostate initalState = velomodel.getInitialState(makeEntry(machine)).getValue();
        // manually find initial state, then test
        Region region = machine.getRegion().iterator().next();
        Pseudostate refInitState = Util.filter(region.getSubvertex(), Pseudostate.class).get(0);
        // test that the two initial states are the equivalent
        Assert.assertEquals("Initial state not equal!",
                refInitState,
                initalState);

        // test for template retrieval of initial state
        Assert.assertEquals("Template test: initial state not equal!",
                refInitState.toString(),
                evalTemplate("$model.getInitialState($model.statemachine).value"));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getSignals(java.util.Entry, boolean)}.
     */
    @Test
    public void testGetSignals () {
        // get the only signal from the velocity model, then from machine
        StateMachine machine = getFirstStateMachine(model);
        Signal signal = velomodel.getSignals(makeEntry(machine), false).iterator().next();
        Signal refSignal = Util.filter(machine.getOwnedMember(), Signal.class).get(0);
        // test for equivalence
        Assert.assertEquals("Signal not equal!",
                refSignal,
                signal);

        // test for template retrieval of signal
        Assert.assertEquals("Template test: signal not equal!",
                refSignal.toString(),
                evalTemplate("$model.getSignals($model.statemachine, false).iterator().next()"));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getType(java.util.Entry)}.
     * Test that getType returns each of 4 UML types and an unknown type.
     * This method depends on 5 other methods, which should be tested to work:
     * {@link VelocityModel#getStatemachine()},
     * {@link VelocityModel#getRegions(Entry, boolean)},
     * {@link VelocityModel#getStates(Entry, boolean)},
     * {@link VelocityModel#getInitialState(Entry)},
     * and {@link VelocityModel#getSignals(Entry, boolean)}.
     */
    @Test
    public void testGetType () {
        // obtain one each of a machine, a region, a state, a pseudostate, and a signal
        StateMachine machine = getFirstStateMachine(model);
        Signal signal = Util.filter(machine.getOwnedMember(), Signal.class).get(0);
        Region region = machine.getRegion().iterator().next();
        State state = Util.filter(region.getSubvertex(), State.class).get(0);
        Pseudostate pseudo = Util.filter(region.getSubvertex(), Pseudostate.class).get(0);
        Assert.assertEquals("A machine returned the wrong type!",
                "statemachine",
                velomodel.getType(makeEntry(machine)));
        Assert.assertEquals("A region returned the wrong type!",
                "region",
                velomodel.getType(makeEntry(region)));
        Assert.assertEquals("A state returned the wrong type!",
                "state",
                velomodel.getType(makeEntry(state)));
        Assert.assertEquals("A pseudostate returned the wrong type!",
                "pseudostate:initial",
                velomodel.getType(makeEntry(pseudo)));
        Assert.assertEquals("A signal should have type 'unknown'!",
                "unknown",
                velomodel.getType(makeEntry(signal)));

        // test velocity template for same output, picking random instances
        Assert.assertEquals("Template test: a machine returned wrong type!",
                "statemachine",
                evalTemplate("$model.getType($model.statemachine)"));
        Assert.assertEquals("Template test: a region returned wrong type!",
                "region",
                evalTemplate("$model.getType($model.getRegions($model.statemachine, false).entrySet().iterator().next())"));
        Assert.assertEquals("Template test: a state returned wrong type!",
                "state",
                evalTemplate("$model.getType($model.getStates($model.statemachine, false).entrySet().iterator().next())"));
        Assert.assertEquals("Template test: a pseudostate returned wrong type!",
                "pseudostate:initial",
                evalTemplate("$model.getType($model.getInitialState($model.statemachine))"));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getNamespace(java.lang.String, gov.nasa.jpl.statechart.uml.NamedElement)}.
     * Checks that any partial prefix matches the same qualified-name/namespace
     * pairing, and that the same partial prefix with a random addition would
     * match the same pairing.  This method depends on tested
     * {@link VelocityModel#getStates(Entry, boolean)} and
     * {@link VelocityModel#getStatemachine()}.
     */
    @Test
    public void testGetNamespace () {
        // get state S2.2 and obtain its qualified name
        Entry<String,State> refPair = getState("T8S22");
        String qname = refPair.getKey();
        State refState = refPair.getValue();
        // generate progressive partial prefix and test that the same pairing is found
        for (int i=1; i < qname.length(); ++i) {
            String prefix = qname.substring(0, i);
            Entry<String,State> pair = velomodel.getNamespace(prefix, refState);
//            System.err.println("With qn '" + prefix + "', got: " + pair.getKey());
            Assert.assertEquals("Pairing with wrong qualified name!",
                    qname,
                    pair.getKey());
            Assert.assertEquals("Pairing with wrong namespace!",
                    refState,
                    pair.getValue());
            /* append a '.', which would not occur in a valid name thus ensuring
             * a non-match, and get same pairing */
            pair = velomodel.getNamespace(prefix + ".", refState);
            Assert.assertEquals("Pairing with wrong qualified name!",
                    qname,
                    pair.getKey());
            Assert.assertEquals("Pairing with wrong namespace!",
                    refState,
                    pair.getValue());
        }
        // TODO is it possible for a state to have 2 qnames, one a prefix of the other?

        /* test that template eval yields same pairing of namespace for S2.2,
         * using object hashCode for state, for both empty prefix and own
         * qname as prefix */
        String refString = qname + refState.toString();
        Assert.assertEquals("Template test: wrong pairing found with empty prefix!",
                refString,
                evalTemplate(velocityGetState("T8S22")
                        + "#set( $_pair = $model.getNamespace(\"\", $_sFound.value) )\n"
                        + "$_pair.key$_pair.value"));
        Assert.assertEquals("Template test: wrong pairing found with own qname!",
                refString,
                evalTemplate(velocityGetState("T8S22")
                        + "#set( $_pair = $model.getNamespace($_sFound.key, $_sFound.value) )\n"
                        + "$_pair.key$_pair.value"));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getExpandedNamespaces(java.lang.String, gov.nasa.jpl.statechart.uml.NamedElement)}.
     */
    @Test
    public void testGetExpandedNamespaces () {
        Random ran = new Random();
        // get state S2.2 and obtain its qualified name
        Entry<String,State> refPair = getState("T8S22");
        String qname = refPair.getKey();
        State refState = refPair.getValue();

        int refCnt = velomodel.getExpandedNamespaces("", refState).size();
        // insert a bogus qname in namespace and test for it
        int branchPtIdx = ran.nextInt(qname.length()-4)+4;
        final String sharedPref = qname.substring(0, branchPtIdx);
        final String bogusQname = sharedPref + ".";
        velomodel.ns2qname.get(refState).add(bogusQname);
        // test that one more namespace is returned up to the full shared prefix
        Assert.assertEquals("Added qname not returned?!",
                refCnt + 1,
                velomodel.getExpandedNamespaces("", refState).size());
        Assert.assertEquals("Added qname not returned?!",
                refCnt + 1,
                velomodel.getExpandedNamespaces(sharedPref, refState).size());
        // test that only one is returned with full qname as well as bogus prefix
        Assert.assertEquals("More than original qname returned!",
                1,
                velomodel.getExpandedNamespaces(qname, refState).size());
        Assert.assertEquals("More than the added qname returned!",
                1,
                velomodel.getExpandedNamespaces(bogusQname, refState).size());
        // remove bogus name
        velomodel.ns2qname.get(refState).remove(bogusQname);
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getOwnedElements(java.util.Entry)}.
     * Checks that the owned elements of the state machine has the same count
     * as {@link VelocityModel#getAllStates(StateMachine)}, and that a leaf
     * state has zero owned element.  Check that the same is true in template
     * evaluation.
     */
    @Test
    public void testGetOwnedElements () {
        // obtain owned elements from machine and test against velocity's result
        StateMachine machine = getFirstStateMachine(model);
        // there should be same count of owned elements as contained regions + states
        Map<String,NamedElement> allStates = velomodel.getAllStates(machine);
        Assert.assertEquals("Machine's owned element count mismatch!",
                allStates.size() - 1 /*machine itself*/,
                velomodel.getOwnedElements(makeEntry(machine)).size());
        // there should be zero child element of state S2.2
        Entry<String,State> s22 = getState("T8S22");
        Assert.assertEquals("Leaf state has owned element?!", 0, velomodel.getOwnedElements(s22).size());

        // test that template output is same
        Assert.assertEquals("Template test: machine's owned element count mismatch!",
                String.valueOf(allStates.size()-1),
                evalTemplate("$model.getOwnedElements($model.statemachine).size()"));
        Assert.assertEquals("Template test: Leaf state has owned element?!",
                "0",
                evalTemplate(velocityGetState("T8S22") /* obtain state S2.2 */
                        + "$model.getOwnedElements($_sFound).size()"));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getOwnedEvents(gov.nasa.jpl.statechart.uml.StateMachine)}.
     * Check that the correct number of owned events of the statemachine are
     * retrieved.  Template eval not tested since this is a protected method.
     */
    @Test
    public void testGetOwnedEvents () {
        // get machine's owned events
        List<Event> events = velomodel.getOwnedEvents(getFirstStateMachine(model));
        // make sure we see 11 events for this test
        Assert.assertEquals("Incorrect count of owned events!",
                11,
                events.size());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getParentState(java.util.Entry)}.
     * Checks that top-state S2 has machine as parent state and S2.2 has S2 as
     * parent state.  Verify that template eval yields the same result.
     */
    @Test
    public void testGetParentState () {
        // get machine, states S2 and S2.2
        StateMachine machine = getFirstStateMachine(model);
        Entry<String,State> s2 = getState("T8S2");
        Entry<String,State> s2c1 = getState("T8S21");
        // check parent state returned is as expected
        Assert.assertEquals("Top state should have state machine as parent state!",
                machine,
                velomodel.getParentState(s2).getValue());
        Assert.assertEquals("Substate's parent state is not found!",
                s2.getValue(),
                velomodel.getParentState(s2c1).getValue());

        // test that template eval yields the same
        Assert.assertEquals("Template test: top state should have state machine as parent state!",
                machine.toString(),
                evalTemplate(velocityGetState("T8S2")
                        + "$model.getParentState($_sFound).value"));
        Assert.assertEquals("Template test: substate's parent state is not found!",
                s2.getValue().toString(),
                evalTemplate(velocityGetState("T8S21")
                        + "$model.getParentState($_sFound).value"));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getStatesWithTimeEventTriggers(java.util.Entry, boolean)}.
     * Check that the two states at the top level with time triggers and the two
     * substates with time triggers are both found.
     */
    @Test
    public void testGetStatesWithTimeEventTriggers () {
        // get the state machine and states S1 and S2
        StateMachine machine = getFirstStateMachine(model);
        Entry<String,State> s1 = getState("T8S1");
        Entry<String,State> s2 = getState("T8S2");
        // from machine level, should yield 4 states
        final int cntFmMachine = 4;
        Assert.assertEquals("Machine has wrong number of states with time event triggers!",
                cntFmMachine,
                velomodel.getStatesWithTimeEventTriggers(makeEntry(machine), false).size());
        // from S1, should yield 1 for S1 itself
        final int cntFmS1 = 1;
        Assert.assertEquals("S1 has wrong number of states with time event triggers!",
                cntFmS1,
                velomodel.getStatesWithTimeEventTriggers(s1, false).size());
        // from S2, should yield 3 for S2 plus two substates
        final int cntFmS2 = 3;
        Assert.assertEquals("S2 has wrong number of states with time event triggers!",
                cntFmS2,
                velomodel.getStatesWithTimeEventTriggers(s2, false).size());

        // test that template eval yields the same
        Assert.assertEquals("Template test: machine has wrong number of states with time event triggers!",
                String.valueOf(cntFmMachine),
                evalTemplate("$model.getStatesWithTimeEventTriggers($model.statemachine, false).size()"));
        Assert.assertEquals("Template test: S1 has wrong number of states with time event triggers!",
                String.valueOf(cntFmS1),
                evalTemplate(velocityGetState("T8S1")
                        + "$model.getStatesWithTimeEventTriggers($_sFound, false).size()"));
        Assert.assertEquals("Template test: S2 has wrong number of states with time event triggers!",
                String.valueOf(cntFmS2),
                evalTemplate(velocityGetState("T8S2")
                        + "$model.getStatesWithTimeEventTriggers($_sFound, false).size()"));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getOutgoingTransitions(java.util.Entry)}.
     * Checks that the outgoing transitions returned via template eval are one
     * and the same as directly accessed from the state.
     */
    @Test
    public void testGetOutgoingTransitions () {
        int cnt = 0;
        // check equivalent output for every single state in machine
        for (Entry<String,State> e
                : velomodel.getStates(velomodel.getStatemachine(), false).entrySet()) {
            State s = e.getValue();
            Assert.assertEquals("Template test: outgoing transitions mismatch for " + s.getName() + "!",
                    s.getOutgoing().toString(),
                    evalTemplate(velocityGetState(s.getName())
                            + "$model.getOutgoingTransitions($_sFound)"));
            ++cnt;
        }
        if (cnt > 0) {
            System.err.println("testGetOutgoingTransitions: Tested outgoing transitions for " + cnt + " states.");
        } else {
            fail("testGetOutgoingTransitions: No outgoing transition tested!");
        }
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getTarget(java.util.Entry, gov.nasa.jpl.statechart.uml.Transition)}.
     * Checks that the target state of every transition from a source state is
     * the same via template eval.  The list of outgoing transitions from a
     * source state should be equivalent whether computed from the model or the
     * template model. Depends on {@link VelocityModel#getOutgoingTransitions(Entry)}
     * to have been tested.
     */
    @Test
    public void testGetTarget () {
        int cnt = 0;
        // check equivalent output for every single state in machine as source
        for (Entry<String,State> e
                : velomodel.getStates(velomodel.getStatemachine(), false).entrySet()) {
            State s = e.getValue();
            // construct output of list of targets from outgoing transitions
            StringBuffer output = new StringBuffer();
            for (Transition t : s.getOutgoing()) {
                output.append(t.getTarget().getQualifiedName()).append("\n");
                ++cnt;
            }
            // via template, we iterate through transitions and produce output
            Assert.assertEquals("Template test: transition target mismatch for " + s.getName() + "!",
                    output.toString(),
                    evalTemplate(velocityGetState(s.getName())
                            + "#foreach( $transition in $model.getOutgoingTransitions($_sFound) )\n"
                            + "$model.getTarget($_sFound, $transition).value.qualifiedName\n"
                            + "#end"));
        }
        if (cnt > 0) {
            System.err.println("testGetTarget: Tested target for " + cnt + " outgoing transitions.");
        } else {
            fail("testGetTarget: No target tested!");
        }
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getTransitionsWithSignalEvent(java.util.Entry)}.
     * Checks that the set of transitions with signal event returned is
     * equivalent for every state in this machine, both via model as well as
     * template model.
     */
    @Test
    public void testGetTransitionsWithSignalEvent () {
        int cnt = 0;
        // check equivalent output for every single state in machine as source
        for (Entry<String,State> e
                : velomodel.getStates(velomodel.getStatemachine(), false).entrySet()) {
            State s = e.getValue();
            Collection<?> tList = velomodel.getTransitionsWithSignalEvent(e);
            if (tList.size() > 0) {
                Assert.assertEquals("Template test: transitions with signal event mismatched for " + s.getName() + "!",
                        tList.toString(),
                        evalTemplate(velocityGetState(s.getName())
                                + "$model.getTransitionsWithSignalEvent($_sFound)"));
                cnt += tList.size();
            }
        }
        if (cnt > 0) {
            System.err.println("testGetTransitionsWithSignalEvent: Tested " + cnt + " transitions.");
        } else {
            fail("testGetTransitionsWithSignalEvent: No valid transition tested!");
        }
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getTransitionsWithSignalEvent(java.util.Entry, boolean)}.
     * Checks that the set of (all) transitions with signal event returned is
     * equivalent for every state in this machine, both via model as well as
     * template model.
     */
    @Test
    public void testGetAllTransitionsWithSignalEvent () {
        int cnt = 0;
        // check equivalent output for every single state in machine as source
        for (Entry<String,State> e
                : velomodel.getStates(velomodel.getStatemachine(), false).entrySet()) {
            State s = e.getValue();
            Collection<?> tList = velomodel.getTransitionsWithSignalEvent(e, true);
            if (tList.size() > 0) {
                Assert.assertEquals("Template test: transitions with signal event mismatched for " + s.getName() + "!",
                        tList.toString(),
                        evalTemplate(velocityGetState(s.getName())
                                + "$model.getTransitionsWithSignalEvent($_sFound, true)"));
                cnt += tList.size();
            }
        }
        if (cnt > 0) {
            System.err.println("testGetAllTransitionsWithSignalEvent: Tested " + cnt + " transitions.");
        } else {
            fail("testGetAllTransitionsWithSignalEvent: No valid transition tested!");
        }
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getTransitionsWithTimeEvent(java.util.Entry)}.
     * Checks that the set of transitions with time event returned is
     * equivalent for every state in this machine, both via model as well as
     * template model.
     */
    @Test
    public void testGetTransitionsWithTimeEvent () {
        int cnt = 0;
        // check equivalent output for every single state in machine as source
        for (Entry<String,State> e
                : velomodel.getStates(velomodel.getStatemachine(), false).entrySet()) {
            State s = e.getValue();
            Collection<?> tList = velomodel.getTransitionsWithTimeEvent(e);
            if (tList.size() > 0) {
                Assert.assertEquals("Template test: transitions with time event mismatched for " + s.getName() + "!",
                        tList.toString(),
                        evalTemplate(velocityGetState(s.getName())
                                + "$model.getTransitionsWithTimeEvent($_sFound)"));
                cnt += tList.size();
            }
        }
        if (cnt > 0) {
            System.err.println("testGetTransitionsWithTimeEvent: Tested " + cnt + " transitions.");
        } else {
            fail("testGetTransitionsWithTimeEvent: No valid transition tested!");
        }
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getTransitionsWithTimeEvent(java.util.Entry, boolean)}.
     * Checks that the set of (all) transitions with time event returned is
     * equivalent for every state in this machine, both via model as well as
     * template model.
     */
    @Test
    public void testGetAllTransitionsWithTimeEvent () {
        int cnt = 0;
        // check equivalent output for every single state in machine as source
        for (Entry<String,State> e
                : velomodel.getStates(velomodel.getStatemachine(), false).entrySet()) {
            State s = e.getValue();
            Collection<?> tList = velomodel.getTransitionsWithTimeEvent(e, true);
            if (tList.size() > 0) {
                Assert.assertEquals("Template test: transitions with time event mismatched for " + s.getName() + "!",
                        tList.toString(),
                        evalTemplate(velocityGetState(s.getName())
                                + "$model.getTransitionsWithTimeEvent($_sFound, true)"));
                cnt += tList.size();
            }
        }
        if (cnt > 0) {
            System.err.println("testGetAllTransitionsWithTimeEvent: Tested " + cnt + " transitions.");
        } else {
            fail("testGetAllTransitionsWithTimeEvent: No valid transition tested!");
        }
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getSignalEvents(gov.nasa.jpl.statechart.uml.Transition)}.
     * Checks that the signal events of every transition in this machine with
     * signal events return the same list of signal events, whether via model
     * or transition model. Depends on {@link VelocityModel#getTransitionsWithSignalEvent(Entry, boolean)}
     * to have been tested.
     */
    @Test
    public void testGetSignalEventsForTransition () {
        int cnt = 0;
        Entry<String,? extends Namespace> machine = makeEntry(getFirstStateMachine(model));
        for (Transition t
                : velomodel.getTransitionsWithSignalEvent(machine, false)) {
            List<SignalEvent> sEvents = new ArrayList<SignalEvent>(t.getSignalEvents());
            // now test against template eval output
            Assert.assertEquals("Template test: signal event(s) mismatched for " + t.id() + "!",
                    sEvents.toString(),
                    evalTemplate("#foreach( $transition in $model.getTransitionsWithSignalEvent($model.statemachine, false) )\n"
                            + "#if( $transition.id().equals(\"" + t.id() + "\") )\n"
                            + "$transition.getSignalEvents()"
                            + "#end\n"
                            + "#end"));
            ++cnt;
        }
        if (cnt > 0) {
            System.err.println("testGetSignalEventsForTransition: Tested " + cnt + " transitions with signal events.");
        } else {
            fail("testGetSignalEventsForTransition: No valid transition tested!");
        }
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getTimeEvents(gov.nasa.jpl.statechart.uml.Transition)}.
     * Checks that the time events of every transition in this machine with
     * time events return the same list of time events, whether via model
     * or transition model. Depends on {@link VelocityModel#getTransitionsWithTimeEvent(Entry, boolean)}
     * to have been tested.
     */
    @Test
    public void testGetTimeEventsForTransition () {
        int cnt = 0;
        Entry<String,? extends Namespace> machine = makeEntry(getFirstStateMachine(model));
        for (Transition t
                : velomodel.getTransitionsWithTimeEvent(machine, false)) {
            List<TimeEvent> sEvents = new ArrayList<TimeEvent>(t.getTimeEvents());
            // now test against template eval output
            Assert.assertEquals("Template test: time event(s) mismatched for " + t.id() + "!",
                    sEvents.toString(),
                    evalTemplate("#foreach( $transition in $model.getTransitionsWithTimeEvent($model.statemachine, false) )\n"
                            + "#if( $transition.id().equals(\"" + t.id() + "\") )\n"
                            + "$transition.getTimeEvents()"
                            + "#end\n"
                            + "#end"));
            ++cnt;
        }
        if (cnt > 0) {
            System.err.println("testGetTimeEventsForTransition: Tested " + cnt + " transitions with time events.");
        } else {
            fail("testGetTimeEventsForTransition: No valid transition tested!");
        }
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getTimeEvents(java.util.Entry)}.
     * Checks that, for every state in this machine, the set of time events that
     * trigger an outgoing transition is the same whether directly retrieved,
     * or evaluated via template.
     */
    @Test
    public void testGetTimeEvents () {
        int cnt = 0;
        // check equivalent output for every single state in machine
        for (Entry<String,State> e
                : velomodel.getStates(velomodel.getStatemachine(), false).entrySet()) {
            State s = e.getValue();
            List<TimeEvent> tEvents = new ArrayList<TimeEvent>();
            for (Transition t : s.getOutgoing()) {
                tEvents.addAll(t.getTimeEvents());
            }
            Assert.assertEquals("Template test: time events mismatched for " + s.getName() + "!",
                    tEvents.toString(),
                    evalTemplate(velocityGetState(s.getName())
                            + "$model.getTimeEvents($_sFound)"));
            ++cnt;
        }
        if (cnt > 0) {
            System.err.println("testGetTimeEvents: Tested time events for " + cnt + " states.");
        } else {
            fail("testGetTimeEvents: No time event tested!");
        }
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getLocalOrthogonalRegions()}.
     * Simply checks that the returned string of regions from template model
     * directly is the same as output via template eval.
     */
    @Test
    public void testGetAllLocalOrthogonalRegions () {
        Assert.assertEquals("Template test: local orthogonal regions mismatched!",
                velomodel.getLocalOrthogonalRegions().toString(),
                evalTemplate("$model.getLocalOrthogonalRegions()"));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getLocalOrthogonalRegions(java.util.Entry)}.
     * Checks that the set of local, orthogonal regions returned from template
     * model directly is the same as via template eval.
     */
    @Test
    public void testGetLocalOrthogonalRegions () {
        int cnt = 0;
        // check equivalent output for every single state in machine
        for (Entry<String,State> e
                : velomodel.getStates(velomodel.getStatemachine(), false).entrySet()) {
            State s = e.getValue();
            Assert.assertEquals("Template test: local orthogonal regions mismatched for " + s.getName() + "!",
                    velomodel.getLocalOrthogonalRegions(e).toString(),
                    evalTemplate(velocityGetState(s.getName())
                            + "$model.getLocalOrthogonalRegions($_sFound)"));
            ++cnt;
        }
        if (cnt > 0) {
            System.err.println("testGetLocalOrthogonalRegions: Tested local, orthogonal region(s) for " + cnt + " states.");
        } else {
            fail("testGetLocalOrthogonalRegions: No valid region tested!");
        }
    }

   /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getEnclosingOrthogonalRegion(java.util.Entry)}.
     * Checks that the set of enclosing, orthogonal regions returned from
     * template model directly is the same as via template eval.
     */
    @Test
    public void testGetEnclosingOrthogonalRegion () {
        int cnt = 0;
        // check equivalent output for every single state in machine
        for (Entry<String,State> e
                : velomodel.getStates(velomodel.getStatemachine(), false).entrySet()) {
            State s = e.getValue();
            Entry<?,?> rEntry = velomodel.getEnclosingOrthogonalRegion(e);
            if (rEntry != null) {
                Assert.assertEquals("Template test: enclosing orthogonal regions mismatched for " + s.getName() + "!",
                        rEntry.getValue().toString(),
                        evalTemplate(velocityGetState(s.getName())
                                + "$model.getEnclosingOrthogonalRegion($_sFound).value"));
                ++cnt;
            }
        }
        if (cnt > 0) {
            System.err.println("testGetEnclosingOrthogonalRegion: Tested enclosing, orthogonal region(s) for " + cnt + " states.");
        } else {
            fail("testGetEnclosingOrthogonalRegion: No valid region tested!");
        }
    }

//    /**
//     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getHistoryContainers()}.
//     * Simply checks that the returned containers from template model directly
//     * is the same as output via template eval.
//     */
//    @Test
//    public void testGetHistoryContainers () {
//        Assert.assertEquals("Template test: history containers mismatched!",
//                velomodel.getHistoryContainers().toString(),
//                evalTemplate("$model.getHistoryContainers()"));
//    }
//
//    /**
//     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getHistoryContainers(gov.nasa.jpl.statechart.uml.Region)}.
//     */
//    @Test
//    public void testGetHistoryContainersRegion () {
//        fail("Not yet implemented");
//    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getSubmachineStates(java.util.Entry, boolean)}.
     * Checks that the set of submachine states returned from template model
     * directly is the same as via template eval.
     */
    @Test
    public void testGetSubmachineStates () {
        int cnt = 0;
        // check equivalent output for every single state in machine
        for (Entry<String,State> e
                : velomodel.getStates(velomodel.getStatemachine(), false).entrySet()) {
            State s = e.getValue();
            Assert.assertEquals("Template test: submachine states mismatched for " + s.getName() + "!",
                    velomodel.getSubmachines(e, false).toString(),
                    evalTemplate(velocityGetState(s.getName())
                            + "$model.getSubmachines($_sFound, false)"));
            ++cnt;
        }
        if (cnt > 0) {
            System.err.println("testGetSubmachineStates: Tested submachine state(s) for " + cnt + " states.");
        } else {
            fail("testGetSubmachineStates: No valid submachine state tested!");
        }
    }

}
